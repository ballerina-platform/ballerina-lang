import ballerina.caching;
import ballerina.runtime;

public function main (string[] args) {
    // Create a new cache. Cache cleanup task runs every 5 seconds and clears
    // any expired cache. So cache expiry time is set to 4 seconds demonstrate
    //cache cleaning.
    caching:Cache cache = caching:createCache("UserCache", 4000, 10, 0.1);
    // Add a new entry to the cache.
    cache.put("Name", "Ballerina");
    // Get the cached value.
    var name, _ = (string)cache.get("Name");
    println("Name: " + name);
    // Sleep the current thread for 6 seconds.
    runtime:sleepCurrentThread(6000);
    // Since the cache expiry time is 4 seconds, cache cleanup task runs at 5th
    // second cleans the cache while this thread was sleeping. Now this value
    // is empty.
    name, _ = (string)cache.get("Name");
    println("Name: " + name);
}
