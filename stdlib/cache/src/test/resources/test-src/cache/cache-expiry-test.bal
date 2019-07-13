import ballerina/cache;

cache:Cache cache = new;

function initCache () {
    cache = new(10000, 10, 0.1);
    cache.put("k1", "v1");
    cache.put("k2", "v2");
    cache.put("k3", "v3");
    cache.put("k4", "v4");
}

function getCacheSize () returns (int) {
    return cache.size();
}
