import ballerina.caching;
import ballerina.runtime;

function testCreateCache (string name, int timeOut, int capacity, float evictionFactor) (string, int, int, float) {
    caching:Cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    return c.name, c.timeOut, c.capacity, c.evictionFactor;
}

function testPut (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:Cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, key, value);
    return c.entries.length();
}

function testGet (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int, string) {
    caching:Cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, key, value);
    var value, e = (string)caching:get(c, key);
    if (e != null) {
        return -1, "";
    }
    return c.entries.length(), value;
}

function testRemove (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:Cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, key, value);
    caching:remove(c, key);
    return c.entries.length();
}

function testCacheEviction (string name, int timeOut, int capacity, float evictionFactor) (string[], int) {
    caching:Cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, "A", "A");
    caching:put(c, "B", "B");
    caching:put(c, "C", "C");
    caching:put(c, "D", "D");
    caching:put(c, "E", "E");
    caching:put(c, "F", "F");
    caching:put(c, "G", "G");
    caching:put(c, "H", "H");
    caching:put(c, "I", "I");
    caching:put(c, "J", "J");
    caching:put(c, "K", "K");
    return c.entries.keys(), c.entries.length();
}

function testCacheClearing () (int, int, int, int, int, int) {
    caching:Cache c1 = caching:createCache("C1", 3000, 10, 0.1);
    caching:put(c1, "k1", "v1");
    caching:put(c1, "k2", "v2");

    caching:Cache c2 = caching:createCache("C2", 7000, 10, 0.1);
    caching:put(c2, "k1", "v1");
    caching:put(c2, "k2", "v2");

    int c1Size1 = c1.entries.length();
    int c2Size1 = c2.entries.length();

    runtime:sleepCurrentThread(6000);

    caching:put(c1, "k3", "v3");
    caching:put(c1, "k4", "v4");
    caching:put(c2, "k3", "v3");
    caching:put(c2, "k4", "v4");

    int c1Size2 = c1.entries.length();
    int c2Size2 = c2.entries.length();

    runtime:sleepCurrentThread(4000);

    int c1Size3 = c1.entries.length();
    int c2Size3 = c2.entries.length();
    return c1Size1, c2Size1, c1Size2, c2Size2, c1Size3, c2Size3;
}
