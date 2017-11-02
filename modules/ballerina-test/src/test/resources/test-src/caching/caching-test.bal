import ballerina.caching;

function testCreateCache (string name, int timeOut, int capacity, float evictionFactor) (string, int, int, float) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    return cache.name, cache.expiryTimeMillis, cache.capacity, cache.evictionFactor;
}

function testPut (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    return cache.size();
}

function testGet (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int, string) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    var value, e = (string)cache.get(key);
    if (e != null) {
        return -1, "";
    }
    return cache.size(), value;
}

function testRemove (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:Cache cache = caching:createCache(name, timeOut, capacity, evictionFactor);
    cache.put(key, value);
    cache.remove(key);
    return cache.size();
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
    return cache.entries.keys(), cache.size();
}

