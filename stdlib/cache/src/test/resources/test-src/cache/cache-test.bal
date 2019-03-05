import ballerina/cache;
import ballerina/runtime;

function testCreateCache(int timeOut, int capacity, float evictionFactor) returns int {
    cache:Cache cache = new(expiryTimeMillis = timeOut, capacity = capacity, evictionFactor = evictionFactor);
    return cache.size();
}

function testPut(string key, string value) returns (int) {
    cache:Cache cache = new;
    cache.put(key, value);
    return cache.size();
}

function testGettingExistingValue(string key, string value) returns (int, string) {
    cache:Cache cache = new;
    cache.put(key, value);
    string returnValue = <string>cache.get(key);
    return (cache.size(), returnValue);
}

function testGettingNonExistingValue(string key) returns any? {
    cache:Cache cache = new;
    any? returnValue = cache.get(key);
    return returnValue;
}

function testRemove(string key, string value) returns (int) {
    cache:Cache cache = new;
    cache.put(key, value);
    cache.remove(key);
    return cache.size();
}

function testCacheEviction1() returns (string[], int) {
    cache:Cache cache = new(expiryTimeMillis = 20000, capacity = 10, evictionFactor = 0.2);
    cache.put("A", "A");
    runtime:sleep(20);
    cache.put("B", "B");
    runtime:sleep(20);
    cache.put("C", "C");
    runtime:sleep(20);
    cache.put("D", "D");
    runtime:sleep(20);
    cache.put("E", "E");
    runtime:sleep(20);
    cache.put("F", "F");
    runtime:sleep(20);
    cache.put("G", "G");
    runtime:sleep(20);
    cache.put("H", "H");
    runtime:sleep(20);
    cache.put("I", "I");
    runtime:sleep(20);
    cache.put("J", "J");
    runtime:sleep(20);
    cache.put("K", "K");
    return (cache.keys(), cache.size());
}

function testCacheEviction2() returns (string[], int) {
    cache:Cache cache = new(expiryTimeMillis = 20000, capacity = 10, evictionFactor = 0.2);
    cache.put("A", "A");
    runtime:sleep(20);
    cache.put("B", "B");
    runtime:sleep(20);
    cache.put("C", "C");
    runtime:sleep(20);
    cache.put("D", "D");
    runtime:sleep(20);
    cache.put("E", "E");
    runtime:sleep(20);
    cache.put("F", "F");
    runtime:sleep(20);
    cache.put("G", "G");
    runtime:sleep(20);
    cache.put("H", "H");
    runtime:sleep(20);
    _ = cache.get("B");
    runtime:sleep(20);
    cache.put("I", "I");
    runtime:sleep(20);
    cache.put("J", "J");
    runtime:sleep(20);
    cache.put("K", "K");
    return (cache.keys(), cache.size());
}

function testCacheEviction3() returns (string[], int) {
    cache:Cache cache = new(expiryTimeMillis = 20000, capacity = 10, evictionFactor = 0.2);
    cache.put("A", "A");
    runtime:sleep(20);
    cache.put("B", "B");
    runtime:sleep(20);
    cache.put("C", "C");
    runtime:sleep(20);
    cache.put("D", "D");
    runtime:sleep(20);
    cache.put("E", "E");
    runtime:sleep(20);
    cache.put("F", "F");
    runtime:sleep(20);
    _ = cache.get("A");
    runtime:sleep(20);
    cache.put("G", "G");
    runtime:sleep(20);
    cache.put("H", "H");
    runtime:sleep(20);
    _ = cache.get("B");
    runtime:sleep(20);
    cache.put("I", "I");
    runtime:sleep(20);
    cache.put("J", "J");
    runtime:sleep(20);
    cache.put("K", "K");
    return (cache.keys(), cache.size());
}

function testCacheEviction4() returns (string[], int) {
    cache:Cache cache = new(expiryTimeMillis = 20000, capacity = 5, evictionFactor = 0.2);
    cache.put("A", "A");
    runtime:sleep(20);
    cache.put("B", "B");
    runtime:sleep(20);
    cache.put("C", "C");
    runtime:sleep(20);
    cache.put("D", "D");
    runtime:sleep(20);
    cache.put("E", "E");
    runtime:sleep(20);
    _ = cache.get("A");
    runtime:sleep(20);
    _ = cache.get("B");
    runtime:sleep(20);
    _ = cache.get("C");
    runtime:sleep(20);
    _ = cache.get("D");
    runtime:sleep(20);
    cache.put("F", "F");
    runtime:sleep(20);
    return (cache.keys(), cache.size());
}

function testExpiredCacheAccess() returns (int) {
    cache:Cache cache = new(expiryTimeMillis = 1000);
    cache.put("A", "A");
    runtime:sleep(2000);
    _ = cache.get("A");
    return cache.size();
}

function testCreateCacheWithZeroExpiryTime() {
    cache:Cache c = new(expiryTimeMillis = 0);
}

function testCreateCacheWithNegativeExpiryTime() {
    cache:Cache c = new(expiryTimeMillis = -10);
}

function testCreateCacheWithZeroCapacity() {
    cache:Cache c = new(capacity = 0);
}

function testCreateCacheWithNegativeCapacity() {
    cache:Cache c = new(capacity = -95);
}

function testCreateCacheWithZeroEvictionFactor() {
    cache:Cache c = new(evictionFactor = 0.0);
}

function testCreateCacheWithInvalidEvictionFactor() {
    cache:Cache c = new(evictionFactor = 1.1);
}
