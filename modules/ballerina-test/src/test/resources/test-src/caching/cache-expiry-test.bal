import ballerina.caching;
import ballerina.runtime;

caching:Cache cache;

function initCache () {
    cache = caching:createCache("C1", 10000, 10, 0.1);
    cache.put("k1", "v1");
    println(cache.entries.length());

    runtime:sleepCurrentThread(2000);
    cache.put("k2", "v2");
    println(cache.entries.length());

    runtime:sleepCurrentThread(2000);
    cache.put("k3", "v3");
    println(cache.entries.length());

    runtime:sleepCurrentThread(2000);
    cache.put("k4", "v4");
    println(cache.entries.length());
}

function getCacheSize () returns (int cacheSize) {
    cacheSize = cache.entries.length();
    println("***********");
    println(cacheSize);
    println("***********");
    return;
}