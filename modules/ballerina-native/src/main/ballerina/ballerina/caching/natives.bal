package ballerina.caching;

import ballerina.lang.maps;
import ballerina.lang.system;

struct cache {
    string name;
    int timeOut;
    int capacity;
    map entries;
}

struct cacheEntry {
    any value;
    int lastAccessedTime;
}

function createCache (string name, int timeOut, int capacity) returns (cache) {
    cache c = {name:name, timeOut:timeOut, capacity:capacity};
    return c;
}

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

function get (cache c, string key) returns (any) {
    var entry, e = (cacheEntry)c.entries[key];
    if (e != null || entry == null) {
        return null;
    }
    entry.lastAccessedTime = system:currentTimeMillis();
    return entry.value;
}

function remove (cache c, string key) {
    maps:remove(c.entries, key);
}

function clear (cache c) {
    c.entries = {};
}

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
