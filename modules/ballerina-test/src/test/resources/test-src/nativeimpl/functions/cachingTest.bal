import ballerina.caching;
import ballerina.lang.maps;
import ballerina.lang.system;

function testCreateCache (string name, int timeOut, int capacity) (string, int, int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    return c.name, c.timeOut, c.capacity;
}

function testPut (string name, int timeOut, int capacity, string key, string value) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, key, value);
    return maps:length(c.entries);
}

function testGet (string name, int timeOut, int capacity, string key, string value) (int, string) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, key, value);
    var value, e = (string)caching:get(c, key);
    if (e != null) {
        return -1, "";
    }
    return maps:length(c.entries), value;
}

function testRemove (string name, int timeOut, int capacity, string key, string value) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, key, value);
    caching:remove(c, key);
    return maps:length(c.entries);
}

function testClear (string name, int timeOut, int capacity) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, "A", "A");
    caching:put(c, "B", "B");
    caching:clear(c);
    return maps:length(c.entries);
}

function testCacheEviction (string name, int timeOut, int capacity) (string[]) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, "A", "A");
    caching:put(c, "B", "B");
    caching:put(c, "C", "C");
    system:println(c.entries);
    return maps:keys(c.entries);
}
