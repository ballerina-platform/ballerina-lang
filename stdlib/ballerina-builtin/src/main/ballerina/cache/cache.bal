// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


import ballerina/system;
import ballerina/task;
import ballerina/time;

@Description {value:"Cache cleanup task starting delay in ms."}
@final int CACHE_CLEANUP_START_DELAY = 0;
@Description {value:"Cache cleanup task invoking interval in ms."}
@final int CACHE_CLEANUP_INTERVAL = 5000;

@Description {value:"Map which stores all of the caches."}
map cacheMap;

@Description {value:"Cleanup task"}
task:Timer timer = createCacheCleanupTask();

@Description {value:"Represents a cache."}
public type Cache object {

    private {
        int capacity;
        map entries;
        int expiryTimeMillis;
        float evictionFactor;
    }

    new(expiryTimeMillis = 900000, capacity = 100, evictionFactor = 0.25) {
        // Cache expiry time must be a positive value.
        if (expiryTimeMillis <= 0) {
            error e = {message:"Expiry time must be greater than 0."};
            throw e;
        }
        // Cache capacity must be a positive value.
        if (capacity <= 0) {
            error e = {message:"Capacity must be greater than 0."};
            throw e;
        }
        // Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).
        if (evictionFactor <= 0 || evictionFactor > 1) {
            error e = {message:"Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive)."};
            throw e;
        }
        cacheMap[system:uuid()] = self;
    }

    public function hasKey(string key) returns (boolean) {
        return self.entries.hasKey(key);
    }

    @Description {value:"Returns the size of the cache."}
    @Return {value:"int: The size of the cache"}
    public function size() returns (int) {
        return lengthof self.entries;
    }

    @Description {value:"Adds the given key, value pair to the provided cache."}
    @Param {value:"key: value which should be used as the key"}
    @Param {value:"value: value to be cached"}
    public function put(string key, any value) {
        int cacheCapacity = self.capacity;
        int cacheSize = lengthof self.entries;

        // if the current cache is full,
        if (cacheCapacity <= cacheSize) {
            self.evictCache();
        }
        // Add the new entry.
        int time = time:currentTime().time;
        CacheEntry entry = {value:value, lastAccessedTime:time};
        self.entries[key] = entry;
    }

    @Description {value:"Evicts the cache when cache is full."}
    function evictCache() {
        int maxCapacity = self.capacity;
        float evictionFactor = self.evictionFactor;
        int numberOfKeysToEvict = <int>(maxCapacity * evictionFactor);
        // Get the above number of least recently used cache entry keys from the cache
        string[] cacheKeys = self.getLRUCacheKeys(numberOfKeysToEvict);
        // Iterate through the map and remove entries.
        foreach c in cacheKeys {
            _ = self.entries.remove(c);
        }
    }

    @Description {value:"Returns the cached value associated with the given key. If the provided cache key is not found,
    an error will be thrown. So use the hasKey function to check for the existance of a particular key."}
    @Param {value:"key: key which is used to retrieve the cached value"}
    @Return {value:"The cached value associated with the given key"}
    public function get(string key) returns any? {
        // Check whether the requested cache is available.
        if (!self.hasKey(key)){
            return ();
        }
        // Get the requested cache entry from the map.
        any value = self.entries[key];
        var entry = <CacheEntry>value;
        // Check whether the cache entry is already expired. Since the cache cleaning task runs in predefined intervals,
        // sometimes the cache entry might not have been removed at this point even though it is expired. So this check
        // gurentees that the expired cache entries will not be returened.
        match (entry) {
            CacheEntry ce => {
                // Get the current system time.
                int currentSystemTime = time:currentTime().time;
                if (currentSystemTime >= ce.lastAccessedTime + expiryTimeMillis) {
                    // If it is expired, remove the cache and return nil.
                    self.remove(key);
                    return ();
                }
                // Modify the last accessed time and return the cache if it is not expired.
                ce.lastAccessedTime = time:currentTime().time;
                return ce.value;
            }
            error => return ();
        }
    }

    @Description {value:"Removes a cached value from a cache."}
    @Param {value:"key: key of the cache entry which needs to be removed"}
    public function remove(string key) {
        _ = self.entries.remove(key);
    }

    public function keys() returns string[] {
        return entries.keys();
    }

    @Description {value:"Returns the key of the Least Recently Used cache entry. This is used to remove cache entries
    if the cache is full."}
    @Return {value:"numberOfKeysToEvict - number of keys to be evicted"}
    function getLRUCacheKeys(int numberOfKeysToEvict) returns (string[]) {
        // Create new arrays to hold keys to be removed and hold the corresponding timestamps.
        string[] cacheKeysToBeRemoved = [];
        int[] timestamps = [];
        map entries = self.entries;
        string[] keys = entries.keys();
        // Iterate through each key.
        foreach key in keys {
            var entry = <CacheEntry>entries[key];
            match (entry) {
                CacheEntry ce => checkAndAdd(numberOfKeysToEvict, cacheKeysToBeRemoved, timestamps, key,
                    ce.lastAccessedTime);
                error => next;
            }
            // Check and add the key to the cacheKeysToBeRemoved if it matches the conditions.
        }
        // Return the array.
        return cacheKeysToBeRemoved;
    }
};

@Description {value:"Represents a cache entry"}
@Field {value:"value: cache value"}
@Field {value:"lastAccessedTime: last accessed time in ms of this value which is used to remove LRU cached values"}
type CacheEntry {
    any value;
    int lastAccessedTime;
};

@Description {value:"Removes expired cache entries from all caches."}
@Return {value:"error: Any error which occured during cache expiration"}
function runCacheExpiry() returns error? {
    // Iterate through all caches.
    foreach currentCacheKey, currentCacheValue in cacheMap {
        var value = <Cache>currentCacheValue;
        match (value) {
            Cache currentCache => {
                // Get the entries in the current cache.
                map currentCacheEntries = currentCache.entries;
                // Ge the keys in the current cache.
                string[] currentCacheEntriesKeys = currentCacheEntries.keys();
                // Get the expiry time of the current cache
                int currentCacheExpiryTime = currentCache.expiryTimeMillis;

                // Create a new array to store keys of cache entries which needs to be removed.
                string[] cachesToBeRemoved = [];
                int cachesToBeRemovedIndex = 0;
                // Iterate through all keys.
                foreach key in currentCacheEntriesKeys {
                    // Get the corresponding entry from the cache.
                    var entry = <CacheEntry>currentCacheEntries[key];
                    match (entry) {
                        CacheEntry ce => {
                            // Get the current system time.
                            int currentSystemTime = time:currentTime().time;
                            // Check whether the cache entry needs to be removed.
                            if (currentSystemTime >= ce.lastAccessedTime + currentCacheExpiryTime) {
                                cachesToBeRemoved[cachesToBeRemovedIndex] = key;
                                cachesToBeRemovedIndex = cachesToBeRemovedIndex + 1;
                            }
                        }
                        error => next;
                    }
                }

                // Iterate through the key list which needs to be removed.
                foreach currentKeyIndex in [0..cachesToBeRemovedIndex) {
                    string key = cachesToBeRemoved[currentKeyIndex];
                    // Remove the cache entry.
                    _ = currentCacheEntries.remove(key);
                }
            }
            error => {
                next;
            }
        }
    }
    return ();
}

function checkAndAdd(int numberOfKeysToEvict, string[] cacheKeys, int[] timestamps, string key, int lastAccessTime) {
    string myKey = key;
    int myLastAccessTime = lastAccessTime;

    // Iterate while we count all values from 0 to numberOfKeysToEvict exclusive of numberOfKeysToEvict since the
    // array size should be numberOfKeysToEvict.
    foreach index in [0..numberOfKeysToEvict) {
        // If we have encountered the end of the array, that means we can add the new values to the end of the
        // array since we haven’t reached the numberOfKeysToEvict limit.
        if (lengthof cacheKeys == index) {
            cacheKeys[index] = myKey;
            timestamps[index] = myLastAccessTime;
            // Break the loop since we don't have any more elements to compare since we are at the end
            break;
        } else {
            // If the timestamps[index] > lastAccessTime, that means the cache which corresponds to the 'key' is
            // older than the current entry at the array which we are checking.
            if (timestamps[index] > myLastAccessTime) {
                // Swap the values. We use the swapped value to continue to check whether we can find any place to
                // add it in the array.
                string tempKey = cacheKeys[index];
                int tempTimeStamp = timestamps[index];
                cacheKeys[index] = myKey;
                timestamps[index] = myLastAccessTime;
                myKey = tempKey;
                myLastAccessTime = tempTimeStamp;
            }
        }
    }
}

@Description {value:"Creates a new cache cleanup task."}
@Return {value:"string: cache cleanup task ID"}
function createCacheCleanupTask() returns task:Timer {
    (function () returns error?) onTriggerFunction = runCacheExpiry;
    task:Timer timer = new(onTriggerFunction, (), CACHE_CLEANUP_INTERVAL, delay = CACHE_CLEANUP_START_DELAY);
    timer.start();
    return timer;
}
