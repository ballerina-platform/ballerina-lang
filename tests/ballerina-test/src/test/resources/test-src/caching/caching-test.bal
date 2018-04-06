import ballerina / caching;
import ballerina / runtime;

function testCreateCache (string name, int timeOut, int capacity, float evictionFactor) returns (string, int, int, float) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    return (cache.name, cache.expiryTimeMillis, cache.capacity, cache.evictionFactor);
}

function testPut (string name, int timeOut, int capacity, float evictionFactor, string key, string value) returns (int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    return cache.size();
}

function testGet (string name, int timeOut, int capacity, float evictionFactor, string key, string value) returns (int, string) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    string returnValue = <string>cache.get(key);
    return (cache.size(), returnValue);
}

function testRemove (string name, int timeOut, int capacity, float evictionFactor, string key, string value) returns (int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    cache.remove(key);
    return cache.size();
}

function testCacheEviction1 (string name, int timeOut, int capacity, float evictionFactor) returns (string[], int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put("A", "A");
    runtime:sleepCurrentWorker(20);
    cache.put("B", "B");
    runtime:sleepCurrentWorker(20);
    cache.put("C", "C");
    runtime:sleepCurrentWorker(20);
    cache.put("D", "D");
    runtime:sleepCurrentWorker(20);
    cache.put("E", "E");
    runtime:sleepCurrentWorker(20);
    cache.put("F", "F");
    runtime:sleepCurrentWorker(20);
    cache.put("G", "G");
    runtime:sleepCurrentWorker(20);
    cache.put("H", "H");
    runtime:sleepCurrentWorker(20);
    cache.put("I", "I");
    runtime:sleepCurrentWorker(20);
    cache.put("J", "J");
    runtime:sleepCurrentWorker(20);
    cache.put("K", "K");
    return (cache.entries.keys(), cache.size());
}

function testCacheEviction2 (string name, int timeOut, int capacity, float evictionFactor) returns (string[], int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put("A", "A");
    runtime:sleepCurrentWorker(20);
    cache.put("B", "B");
    runtime:sleepCurrentWorker(20);
    cache.put("C", "C");
    runtime:sleepCurrentWorker(20);
    cache.put("D", "D");
    runtime:sleepCurrentWorker(20);
    cache.put("E", "E");
    runtime:sleepCurrentWorker(20);
    cache.put("F", "F");
    runtime:sleepCurrentWorker(20);
    cache.put("G", "G");
    runtime:sleepCurrentWorker(20);
    cache.put("H", "H");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("B");
    runtime:sleepCurrentWorker(20);
    cache.put("I", "I");
    runtime:sleepCurrentWorker(20);
    cache.put("J", "J");
    runtime:sleepCurrentWorker(20);
    cache.put("K", "K");
    return (cache.entries.keys(), cache.size());
}

function testCacheEviction3 (string name, int timeOut, int capacity, float evictionFactor) returns (string[], int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put("A", "A");
    runtime:sleepCurrentWorker(20);
    cache.put("B", "B");
    runtime:sleepCurrentWorker(20);
    cache.put("C", "C");
    runtime:sleepCurrentWorker(20);
    cache.put("D", "D");
    runtime:sleepCurrentWorker(20);
    cache.put("E", "E");
    runtime:sleepCurrentWorker(20);
    cache.put("F", "F");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("A");
    runtime:sleepCurrentWorker(20);
    cache.put("G", "G");
    runtime:sleepCurrentWorker(20);
    cache.put("H", "H");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("B");
    runtime:sleepCurrentWorker(20);
    cache.put("I", "I");
    runtime:sleepCurrentWorker(20);
    cache.put("J", "J");
    runtime:sleepCurrentWorker(20);
    cache.put("K", "K");
    return (cache.entries.keys(), cache.size());
}

function testCacheEviction4 (string name, int timeOut, int capacity, float evictionFactor) returns (string[], int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put("A", "A");
    runtime:sleepCurrentWorker(20);
    cache.put("B", "B");
    runtime:sleepCurrentWorker(20);
    cache.put("C", "C");
    runtime:sleepCurrentWorker(20);
    cache.put("D", "D");
    runtime:sleepCurrentWorker(20);
    cache.put("E", "E");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("A");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("B");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("C");
    runtime:sleepCurrentWorker(20);
    _ = cache.get("D");
    runtime:sleepCurrentWorker(20);
    cache.put("F", "F");
    runtime:sleepCurrentWorker(20);
    return (cache.entries.keys(), cache.size());
}

function testCreateCacheWithZeroExpiryTime () {
    _ = caching:createCache("test", 0, 10, 0.1);
}

function testCreateCacheWithNegativeExpiryTime () {
    _ = caching:createCache("test", -10, 10, 0.1);
}

function testCreateCacheWithZeroCapacity () {
    _ = caching:createCache("test", 10000, 0, 0.1);
}

function testCreateCacheWithNegativeCapacity () {
    _ = caching:createCache("test", 10000, -10, 0.1);
}

function testCreateCacheWithZeroEvictionFactor () {
    _ = caching:createCache("test", 10000, 10, 0);
}

function testCreateCacheWithInvalidEvictionFactor () {
    _ = caching:createCache("test", 10000, 10, 1.1);
}
