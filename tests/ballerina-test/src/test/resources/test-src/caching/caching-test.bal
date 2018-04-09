import ballerina/caching;
import ballerina/runtime;

function testCreateCache(int timeOut, int capacity, float evictionFactor) returns int {
    caching:Cache cache = new(expiryTimeMillis = timeOut, capacity = capacity, evictionFactor = evictionFactor);
    return cache.size();
}

function testPut(string key, string value) returns (int) {
    caching:Cache cache = new;
    cache.put(key, value);
    return cache.size();
}

function testGet(string key, string value) returns (int, string) {
    caching:Cache cache = new;
    cache.put(key, value);
    string returnValue = <string>cache.get(key);
    return (cache.size(), returnValue);
}

function testRemove(string key, string value) returns (int) {
    caching:Cache cache = new;
    cache.put(key, value);
    cache.remove(key);
    return cache.size();
}

function testCacheEviction1() returns (string[], int) {
    caching:Cache cache = new (expiryTimeMillis = 20000, capacity = 10, evictionFactor = 0.2);
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
    return (cache.keys(), cache.size());
}

function testCacheEviction2() returns (string[], int) {
    caching:Cache cache = new (expiryTimeMillis = 20000, capacity = 10, evictionFactor = 0.2);
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
    return (cache.keys(), cache.size());
}

function testCacheEviction3() returns (string[], int) {
    caching:Cache cache = new (expiryTimeMillis = 20000, capacity = 10, evictionFactor = 0.2);
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
    return (cache.keys(), cache.size());
}

function testCacheEviction4() returns (string[], int) {
    caching:Cache cache = new (expiryTimeMillis = 20000, capacity = 5, evictionFactor = 0.2);
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
    return (cache.keys(), cache.size());
}

function testCreateCacheWithZeroExpiryTime() {
    caching:Cache c = new(expiryTimeMillis = 0);
}

function testCreateCacheWithNegativeExpiryTime() {
    caching:Cache c = new(expiryTimeMillis = 10);
}

function testCreateCacheWithZeroCapacity() {
    caching:Cache c = new(capacity = 0);
}

function testCreateCacheWithNegativeCapacity() {
    caching:Cache c = new(capacity = -95);
}

function testCreateCacheWithZeroEvictionFactor() {
    caching:Cache c = new(evictionFactor = 0);
}

function testCreateCacheWithInvalidEvictionFactor() {
    caching:Cache c = new(evictionFactor = 1.1);
}
