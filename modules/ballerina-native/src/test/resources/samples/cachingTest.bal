import ballerina.caching;

function testCreateCache (string name, int timeOut, int capacity) (string, int, int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    return c.name, c.timeOut, c.capacity;
}

function testPush (string name, int timeOut, int capacity, string key, string value) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, key, value);
    return c.capacity;
}

function testGet (string name, int timeOut, int capacity, string key, string value) (int, string) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, key, value);
    var value, e = (string)caching:get(c, key);
    return c.capacity, value;
}

function testRemove (string name, int timeOut, int capacity, string key, string value) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, key, value);
    caching:remove(c, key);
    return c.capacity;
}

function testClear (string name, int timeOut, int capacity) (int) {
    caching:cache c = caching:createCache(name, timeOut, capacity);
    caching:put(c, "A", "A");
    caching:put(c, "B", "B");
    caching:clear(c);
    return c.capacity;
}
