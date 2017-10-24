import ballerina.caching;
import ballerina.lang.maps;
import ballerina.lang.system;

function testCreateCache (string name, int timeOut, int capacity, float evictionFactor) (string, int, int, float) {
    caching:cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    return c.name, c.timeOut, c.capacity, c.evictionFactor;
}

function testPut (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, key, value);
    return maps:length(c.entries);
}

function testGet (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int, string) {
    caching:cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, key, value);
    var value, e = (string)caching:get(c, key);
    if (e != null) {
        return -1, "";
    }
    return maps:length(c.entries), value;
}

function testRemove (string name, int timeOut, int capacity, float evictionFactor, string key, string value) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, key, value);
    caching:remove(c, key);
    return maps:length(c.entries);
}

function testClear (string name, int timeOut, int capacity, float evictionFactor) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
    caching:put(c, "A", "A");
    caching:put(c, "B", "B");
    caching:clear(c);
    return maps:length(c.entries);
}

function testCacheEviction (string name, int timeOut, int capacity, float evictionFactor) (string[], int) {
    caching:cache c = caching:createCache(name, timeOut, capacity, evictionFactor);
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
    system:println(c.entries);
    return maps:keys(c.entries), maps:length(c.entries);
}
