import ballerina/cache;
import ballerina/io;
import ballerina/runtime;

public function main() returns error? {
    // This creates a new cache of size 10. The eviction factor is set to 0.2,
    // which means at the time of eviction 10*0.2=2 entries get removed from
    // the cache.
    // The default max age of the cache entry is set to 2 seconds. The cache
    // cleanup task runs every 3 seconds and clears all the expired entries.
    cache:Cache cache = new({
        capacity: 10,
        evictionFactor: 0.2,
        defaultMaxAgeInSeconds: 2,
        cleanupIntervalInSeconds: 3
    });

    // Adds a new entry to the cache.
    _ = check cache.put("key1", "value1");
    // Adds a new entry to the cache by overriding the default max age.
    _ = check cache.put("key2", "value2", 3600);

    // Checks for the cached key availability.
    if (cache.hasKey("key1")) {
        // Fetches the cached value.
        string value = <string> check cache.get("key1");
        io:println("key1: " + value);
    }

    // This sends the current worker to the sleep mode for 4 seconds.
    // No execution takes place during this period.
    runtime:sleep(4000);

    // During this period, the cache entry with the key 'key1' should be removed
    // since the max of it is set to 2 seconds by default. However, the cache
    // entry with the key 'key2' should exist in the cache.

    // Get the keys of the cache entries.
    string[] keys = cache.keys();
    io:println("keys: [" + keys.toString() + "]");

    // Get the size of the cache.
    int size = cache.size();

    // Get the capacity of the cache.
    int capacity = cache.capacity();

    // Discard the given cache entry.
    _ = check cache.invalidate("key2");

    // Discard all the cache entries of the cache.
    _ = check cache.invalidateAll();
}
