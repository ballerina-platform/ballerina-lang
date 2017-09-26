package ballerina.caching;

import ballerina.doc;
import ballerina.lang.maps;
import ballerina.lang.system;

@doc:Description {value:"Represents a cache."}
@doc:Field {value:"name - name of the cache"}
@doc:Field {value:"timeout - timeout of the cache"}
@doc:Field {value:"capacity - capacity of the cache"}
@doc:Field {value:"entries - map which contains the cache entries"}
struct cache {
    string name;
    int timeOut;
    int capacity;
    map entries;
}

@doc:Description {value:"Represents a cache entry"}
@doc:Field {value:"value - cache value"}
@doc:Field {value:"lastAccessedTime - last accessed time of this value which is used to remove LRU cached values"}
struct cacheEntry {
    any value;
    int lastAccessedTime;
}

@doc:Description {value:"Creates a new cache."}
@doc:Param {value:"name - name of the cache"}
@doc:Param {value:"timeout - timeout of the cache"}
@doc:Param {value:"capacity - capacitry of the cache"}
@doc:Return {value:"cache - a new cache"}
function createCache (string name, int timeOut, int capacity) returns (cache) {
    map entries = {};
    cache c = {name:name, timeOut:timeOut, capacity:capacity, entries:entries};
    return c;
}

@doc:Description {value:"Adds the given key, value pair to the provided cache."}
@doc:Param {value:"cache - a cache"}
@doc:Param {value:"key - value which should be used as the key"}
@doc:Param {value:"value - value to be cached"}
function put (cache c, string key, any value) {
    int maxCapacity = c.capacity;
    int currentCapacity = maps:length(c.entries);
    if (maxCapacity <= currentCapacity) {
        string cacheKey = getLRUCache(c);
        maps:remove(c.entries, cacheKey);
    }

    int currentTime = system:currentTimeMillis();
    cacheEntry entry = {value:value, lastAccessedTime:currentTime};
    c.entries[key] = entry;
}

@doc:Description {value:"Returns the cached value associated with the given key. Returns null if the provided key does not exist in the cache."}
@doc:Param {value:"cache - a cache"}
@doc:Param {value:"key - key which is used to retrieve the cached value"}
function get (cache c, string key) returns (any) {
    any value = c.entries[key];
    if (value == null) {
        return null;
    }
    var entry, e = (cacheEntry)value;
    if (e != null || entry == null) {
        return null;
    }
    entry.lastAccessedTime = system:currentTimeMillis();
    return entry.value;
}

@doc:Description {value:"Removes a cached value from a cache."}
@doc:Param {value:"cache - a cache"}
@doc:Param {value:"key - key of the cache entry which needs to be removed"}
function remove (cache c, string key) {
    maps:remove(c.entries, key);
}

@doc:Description {value:"Clears a cache."}
@doc:Param {value:"cache - a cache"}
function clear (cache c) {
    c.entries = {};
}

@doc:Description {value:"Returns the key of the Least Recently Used cache entry. This is used to remove cache entries if the cache is full."}
@doc:Param {value:"cache - a cache"}
@doc:Return {value:"string - key of the LRU cache entry"}
function getLRUCache (cache c) (string cacheKey) {
    map entries = c.entries;
    string[] keys = maps:keys(entries);
    cacheKey = "";
    int currentMin = system:currentTimeMillis();

    int index = 0;
    int size = lengthof keys;

    while (index < size) {
        string key = keys[index];
        var entry, _ = (cacheEntry)entries[key];
        if (currentMin < entry.lastAccessedTime) {
            cacheKey = key;
            currentMin = entry.lastAccessedTime;
        }
        index = index + 1;
    }
    return cacheKey;
}
