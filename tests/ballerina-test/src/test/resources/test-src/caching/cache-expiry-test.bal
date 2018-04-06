import ballerina/caching;

caching:Cache cache;

function initCache() {
    cache = caching:createCache("C1", 10000, 10, 0.1);
    cache.put("k1", "v1");
    cache.put("k2", "v2");
    cache.put("k3", "v3");
    cache.put("k4", "v4");
}

function getCacheSize() returns (int) {
    return cache.size();
}
