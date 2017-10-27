import ballerina.caching;
import ballerina.runtime;

function testCreateCache (string name, int timeOut, int capacity, float evictionFactor) (string, int, int, float) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    return cache.name, cache.timeOut, cache.capacity, cache.evictionFactor;
}

function testPut (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    return cache.entries.length();
}

function testGet (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int, string) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    var value, e = (string)cache.get(key);
    if (e != null) {
        return -1, "";
    }
    return cache.entries.length(), value;
}

function testRemove (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    cache.remove(key);
    return cache.entries.length();
}

function testCacheEviction (string name, int timeOut, int capacity, float evictionFactor) (string[], int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put("A", "A");
    cache.put("B", "B");
    cache.put("C", "C");
    cache.put("D", "D");
    cache.put("E", "E");
    cache.put("F", "F");
    cache.put("G", "G");
    cache.put("H", "H");
    cache.put("I", "I");
    cache.put("J", "J");
    cache.put("K", "K");
    return cache.entries.keys(), cache.entries.length();
}

function testCacheClearing () (int, int, int, int, int, int) {
    caching:Cache cache1 = caching:createCache("C1", 3000, 10, 0.1);
    cache1.put("k1", "v1");
    cache1.put("k2", "v2");

    caching:Cache cache2 = caching:createCache("C2", 7000, 10, 0.1);
    cache2.put("k1", "v1");
    cache2.put("k2", "v2");

    int c1Size1 = cache1.entries.length();
    int c2Size1 = cache2.entries.length();

    runtime:sleepCurrentThread(6000);

    cache1.put("k3", "v3");
    cache1.put("k4", "v4");
    cache2.put("k3", "v3");
    cache2.put("k4", "v4");

    int c1Size2 = cache1.entries.length();
    int c2Size2 = cache2.entries.length();

    runtime:sleepCurrentThread(4000);

    int c1Size3 = cache1.entries.length();
    int c2Size3 = cache2.entries.length();
    return c1Size1, c2Size1, c1Size2, c2Size2, c1Size3, c2Size3;
}
