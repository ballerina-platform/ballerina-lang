package ballerina.caching;

import ballerina.doc;
import ballerina.task;

@doc:Description {value:"delay in ms which is used to create a new cache cleanup task."}
const int CACHE_CLEANUP_START_DELAY = 0;
@doc:Description {value:"interval in ms which is used to create a new cache cleanup task."}
const int CACHE_CLEANUP_INTERVAL = 5000;

@doc:Description {value:"array which stores the caches."}
Cache[] caches = [];

string scheduleID;

@doc:Description {value:"Represents a cache."}
@doc:Field {value:"name - name of the cache"}
@doc:Field {value:"timeout - timeout of the cache in seconds"}
@doc:Field {value:"capacity - capacity of the cache"}
@doc:Field {value:"evictionFactor - eviction factor to be used for cache eviction"}
@doc:Field {value:"entries - map which contains the cache entries"}
public struct Cache {
    string name;
    int timeOut;
    int capacity;
    float evictionFactor;
    map entries;
}

@doc:Description {value:"Represents a cache entry"}
@doc:Field {value:"value - cache value"}
@doc:Field {value:"lastAccessedTime - last accessed time(in nano seconds) of this value which is used to remove LRU cached values"}
struct CacheEntry {
    any value;
    int lastAccessedTime;
}

@doc:Description {value:"Creates a new cache."}
@doc:Param {value:"name - name of the cache"}
@doc:Param {value:"timeout - timeout of the cache in seconds"}
@doc:Param {value:"capacity - capacitry of the cache which should be greater than 0"}
@doc:Param {value:"evictionFactor - eviction factor to be used for cache eviction"}
@doc:Return {value:"cache - a new cache"}
public function createCache (string name, int timeOut, int capacity, float evictionFactor) returns (Cache) {
    // Cache capacity must be a positive value.
    if (capacity <= 0) {
        error e = {msg:"Capacity must be greater than 0."};
        throw e;
    }

    // If a cache cleanup scheduler is not already created, create a new scheduler.
    if (scheduleID == "") {
        function () returns (error) onTriggerFunction = cleanCache;
        function (error) onErrorFunction = null;
        error schedulerError;
        scheduleID, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:CACHE_CLEANUP_START_DELAY, interval:CACHE_CLEANUP_INTERVAL});
        // If task creation failed, throw an error.
        if (schedulerError != null) {
            throw schedulerError;
        }
    }

    // Create a new cache.
    Cache c = {name:name, timeOut:timeOut, capacity:capacity, evictionFactor:evictionFactor, entries:{}};
    // Get the current total cache count.
    int currentCachesCount = lengthof caches;
    // Add the new cache to the end of the caches array.
    caches[currentCachesCount] = c;
    // Return the new cache.
    return c;
}

@doc:Description {value:"Adds the given key, value pair to the provided cache."}
@doc:Param {value:"cache - a cache"}
@doc:Param {value:"key - value which should be used as the key"}
@doc:Param {value:"value - value to be cached"}
public function put (Cache c, string key, any value) {
    int maxCapacity = c.capacity;
    int currentCapacity = c.entries.length();
    // if the current cache is full,
    if (maxCapacity <= currentCapacity) {
        evictCache(c);
    }
    // Add the new entry
    int time = currentTime().time;
    CacheEntry entry = {value:value, lastAccessedTime:time};
    c.entries[key] = entry;
}

function evictCache (Cache c) {
    int maxCapacity = c.capacity;
    float evictionFactor = c.evictionFactor;
    int noOfEntriesToBeEvicted = <int>(maxCapacity * evictionFactor);
    int i = 0;
    while (i < noOfEntriesToBeEvicted) {
        string cacheKey = getLRUCache(c);
        c.entries.remove(cacheKey);
        i = i + 1;
    }
}

@doc:Description {value:"Returns the cached value associated with the given key. Returns null if the provided key does not exist in the cache."}
@doc:Param {value:"cache - a cache"}
@doc:Param {value:"key - key which is used to retrieve the cached value"}
public function get (Cache c, string key) returns (any) {
    any value = c.entries[key];
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

@doc:Description {value:"Removes a cached value from a cache."}
@doc:Param {value:"cache - a cache"}
@doc:Param {value:"key - key of the cache entry which needs to be removed"}
public function remove (Cache c, string key) {
    c.entries.remove(key);
}

@doc:Description {value:"Clears a cache."}
public function cleanCache () returns (error) {
    int currentCacheIndex = 0;
    int cacheSize = lengthof caches;
    // Iterate through all caches.
    while (currentCacheIndex < cacheSize) {
        // Get a cache from the array.
        Cache currentCache = caches[currentCacheIndex];
        // If the cache is null, go to next cache.
        if (currentCache == null) {
            next;
        }
        // Get the entries in the current cache.
        map currentCacheEntries = currentCache.entries;
        // Ge the keys in the current cache.
        string[] currentCacheEntriesKeys = currentCacheEntries.keys();
        // Get the timeout of the current cache
        int currentCacheTimeout = currentCache.timeOut;

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
            if (currentSystemTime >= entry.lastAccessedTime + currentCacheTimeout) {
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

@doc:Description {value:"Returns the key of the Least Recently Used cache entry. This is used to remove cache entries if the cache is full."}
@doc:Param {value:"cache - a cache"}
@doc:Return {value:"string - key of the LRU cache entry"}
public function getLRUCache (Cache c) (string cacheKey) {
    map entries = c.entries;
    string[] keys = entries.keys();
    cacheKey = "";
    int currentMinimumTime = currentTime().time;

    int index = 0;
    int size = lengthof keys;

    while (index < size) {
        string key = keys[index];
        var entry, _ = (CacheEntry)entries[key];
        if (currentMinimumTime > entry.lastAccessedTime) {
            cacheKey = key;
            currentMinimumTime = entry.lastAccessedTime;
        }
        index = index + 1;
    }
    return cacheKey;
}
