package ballerina.caching;

import ballerina/task;
import ballerina/util;

@Description {value:"Cache cleanup task starting delay in ms."}
const int CACHE_CLEANUP_START_DELAY = 0;
@Description {value:"Cache cleanup task invoking interval in ms."}
const int CACHE_CLEANUP_INTERVAL = 5000;

@Description {value:"Map which stores all of the caches."}
map cacheMap = {};

@Description {value:"Cleanup task ID."}
string cacheCleanupTaskID = createCacheCleanupTask();

@Description {value:"Represents a cache."}
@Field {value:"name: name of the cache"}
@Field {value:"expiryTimeMillis: cache expiry time in ms"}
@Field {value:"capacity: capacity of the cache"}
@Field {value:"evictionFactor: eviction factor to be used for cache eviction"}
@Field {value:"entries: map which contains the cache entries"}
public struct Cache {
    string name;
    int expiryTimeMillis;
    int capacity;
    float evictionFactor;
    map entries;
}

@Description {value:"Represents a cache entry"}
@Field {value:"value: cache value"}
@Field {value:"lastAccessedTime: last accessed time in ms of this value which is used to remove LRU cached values"}
struct CacheEntry {
    any value;
    int lastAccessedTime;
}

@Description {value:"Creates a new cache."}
@Param {value:"name: name of the cache"}
@Param {value:"expiryTimeMillis: expiryTime of the cache in ms"}
@Param {value:"capacity: capacitry of the cache which should be greater than 0"}
@Param {value:"evictionFactor: eviction factor to be used for cache eviction"}
@Return {value:"cache: a new cache"}
public function createCache (string name, int expiryTimeMillis, int capacity, float evictionFactor) returns (Cache) {
    // Cache expiry time must be a positive value.
    if (expiryTimeMillis <= 0) {
        error e = {msg:"Expiry time must be greater than 0."};
        throw e;
    }
    // Cache capacity must be a positive value.
    if (capacity <= 0) {
        error e = {msg:"Capacity must be greater than 0."};
        throw e;
    }
    // Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).
    if (evictionFactor <= 0 || evictionFactor > 1) {
        error e = {msg:"Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive)."};
        throw e;
    }

    // Create a new cache.
    Cache cache = {name:name, expiryTimeMillis:expiryTimeMillis, capacity:capacity, evictionFactor:evictionFactor, entries:{}};
    // Add the new cache to the map.
    cacheMap[util:uuid()] = cache;
    // Return the new cache.
    return cache;
}

@Description {value:"Returns the size of the cache."}
@Return {value:"int: The size of the cache"}
public function <Cache cache> size () returns (int) {
    return lengthof cache.entries;
}

@Description {value:"Adds the given key, value pair to the provided cache."}
@Param {value:"key: value which should be used as the key"}
@Param {value:"value: value to be cached"}
public function <Cache cache> put (string key, any value) {
    int cacheCapacity = cache.capacity;
    int cacheSize = lengthof cache.entries;
    // if the current cache is full,
    if (cacheCapacity <= cacheSize) {
        cache.evictCache();
    }
    // Add the new entry.
    int time = currentTime().time;
    CacheEntry entry = {value:value, lastAccessedTime:time};
    cache.entries[key] = entry;
}

@Description {value:"Evicts the cache when cache is full."}
function <Cache cache> evictCache () {
    int maxCapacity = cache.capacity;
    float evictionFactor = cache.evictionFactor;
    int numberOfKeysToEvict = <int>(maxCapacity * evictionFactor);
    // Get the above number of least recently used cache entry keys from the cache.
    string[] cacheKeys = cache.getLRUCacheKeys(numberOfKeysToEvict);
    // Iterate through the map and remove entries.
    int index = 0;
    while (index < (lengthof cacheKeys)) {
        cache.entries.remove(cacheKeys[index]);
        index = index + 1;
    }
}

@Description {value:"Returns the cached value associated with the given key. Returns null if the provided key does not exist in the cache."}
@Param {value:"key: key which is used to retrieve the cached value"}
@Return { value:"The cached value associated with the given key" }
public function <Cache cache> get (string key) returns (any) {
    any value = cache.entries[key];
    if (value == null) {
        return null;
    }
    var entry, e = (CacheEntry)value;
    if (e != null || entry == null) {
        return null;
    }
    entry.lastAccessedTime = currentTime().time;
    return entry.value;
}

@Description {value:"Removes a cached value from a cache."}
@Param {value:"key: key of the cache entry which needs to be removed"}
public function <Cache cache> remove (string key) {
    cache.entries.remove(key);
}

@Description {value:"Removes expired cache entries from all caches."}
@Return {value:"error: Any error which occured during cache expiration"}
function runCacheExpiry () returns (error) {
    int currentCacheIndex = 0;
    int cacheSize = lengthof cacheMap;
    // Iterate through all caches.
    while (currentCacheIndex < cacheSize) {
        string currentCacheKey = cacheMap.keys()[currentCacheIndex];
        // Get a cache from the array.
        var currentCache, err = (Cache)cacheMap[currentCacheKey];
        if (err != null) {
            next;
        }
        // If the cache is null, go to next cache.
        if (currentCache == null) {
            next;
        }
        // Get the entries in the current cache.
        map currentCacheEntries = currentCache.entries;
        // Ge the keys in the current cache.
        string[] currentCacheEntriesKeys = currentCacheEntries.keys();
        // Get the expiry time of the current cache
        int currentCacheExpiryTime = currentCache.expiryTimeMillis;

        int currentKeyIndex = 0;
        int currentCacheSize = lengthof currentCacheEntriesKeys;

        // Create a new array to store keys of cache entries which needs to be removed.
        string[] cachesToBeRemoved = [];
        int cachesToBeRemovedIndex = 0;
        // Iterate through all keys.
        while (currentKeyIndex < currentCacheSize) {
            // Get the current key.
            string key = currentCacheEntriesKeys[currentKeyIndex];
            // Get the corresponding entry from the cache.
            var entry, _ = (CacheEntry)currentCacheEntries[key];
            // Get the current system time.
            int currentSystemTime = currentTime().time;
            // Check whether the cache entry needs to be removed.
            if (currentSystemTime >= entry.lastAccessedTime + currentCacheExpiryTime) {
                cachesToBeRemoved[cachesToBeRemovedIndex] = key;
                cachesToBeRemovedIndex = cachesToBeRemovedIndex + 1;
            }
            currentKeyIndex = currentKeyIndex + 1;
        }

        // Iterate through the key list which needs to be removed.
        currentKeyIndex = 0;
        while (currentKeyIndex < cachesToBeRemovedIndex) {
            string key = cachesToBeRemoved[currentKeyIndex];
            // Remove the cache entry.
            currentCacheEntries.remove(key);
            currentKeyIndex = currentKeyIndex + 1;
        }
        currentCacheIndex = currentCacheIndex + 1;
    }
    return null;
}

@Description {value:"Returns the key of the Least Recently Used cache entry. This is used to remove cache entries if the cache is full."}
@Return {value:"numberOfKeysToEvict - number of keys to be evicted"}
function <Cache cache> getLRUCacheKeys (int numberOfKeysToEvict) (string[]) {
    // Create new arrays to hold keys to be removed and hold the corresponding timestamps.
    string[] cacheKeysToBeRemoved = [];
    int[] timestamps = [];
    map entries = cache.entries;
    string[] keys = entries.keys();
    int size = lengthof keys;
    // Iterate through each key.
    int index = 0;
    while (index < size) {
        string key = keys[index];
        var entry, _ = (CacheEntry)entries[key];
        // Check and add the key to the cacheKeysToBeRemoved if it matches the conditions.
        checkAndAdd(numberOfKeysToEvict, cacheKeysToBeRemoved, timestamps, key, entry.lastAccessedTime);
        index = index + 1;
    }
    // Return the array.
    return cacheKeysToBeRemoved;
}

function checkAndAdd (int numberOfKeysToEvict, string[] cacheKeys, int[] timestamps, string key, int lastAccessTime) {
    int index = 0;
    // Iterate while we count all values from 0 to numberOfKeysToEvict exclusive of numberOfKeysToEvict since the
    // array size should be numberOfKeysToEvict.
    while (index < numberOfKeysToEvict) {
        // If we have encountered the end of the array, that means we can add the new values to the end of the
        // array since we haven’t reached the numberOfKeysToEvict limit.
        if (lengthof cacheKeys == index) {
            cacheKeys[index] = key;
            timestamps[index] = lastAccessTime;
            // Break the loop since we don't have any more elements to compare since we are at the end
            break;
        } else {
            // If the timestamps[index] > lastAccessTime, that means the cache which corresponds to the 'key' is
            // older than the current entry at the array which we are checking.
            if (timestamps[index] > lastAccessTime) {
                // Swap the values. We use the swapped value to continue to check whether we can find any place to
                // add it in the array.
                string tempKey = cacheKeys[index];
                int tempTimeStamp = timestamps[index];
                cacheKeys[index] = key;
                timestamps[index] = lastAccessTime;
                key = tempKey;
                lastAccessTime = tempTimeStamp;
            }
        }
        index = index + 1;
    }
}

@Description {value:"Creates a new cache cleanup task."}
@Return {value:"string: cache cleanup task ID"}
function createCacheCleanupTask () (string) {
    function () returns (error) onTriggerFunction = runCacheExpiry;
    function (error) onErrorFunction = null;
    var cacheCleanupTaskID, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:CACHE_CLEANUP_START_DELAY, interval:CACHE_CLEANUP_INTERVAL});
    // If task creation failed, throw an error.
    if (schedulerError != null) {
        throw schedulerError;
    }
    return cacheCleanupTaskID;
}
