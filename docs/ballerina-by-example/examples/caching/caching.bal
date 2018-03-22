import ballerina/caching;
import ballerina/io;
import ballerina/runtime;

public function main (string[] args) {
    // Create a new cache. Cache cleanup task runs every 5 seconds and clears
    // any expired cache. So cache expiry time is set to 4 seconds to demonstrate
    // cache cleaning.
    caching:Cache cache = caching:createCache("UserCache", 4000, 10, 0.1);
    // Add a new entry to the cache.
    cache.put("Name", "Ballerina");
    // Get the cached value.
    var name =? <string>cache.get("Name");
    io:println("Name: " + name);
    // Sends the current worker to sleep mode for 6 seconds; no execution takes place during this period.
    runtime:sleepCurrentWorker(6000);
    // Since the cache expiry time is 4 seconds, the cache cleanup task runs at the 5th
    // second and cleans the cache while this thread was sleeping.

    //try {
    //    var cacheReturn = cache.get("Name");
    //} catch (error err) {
    //    io:println("error occurred while processing chars " + err.message);
    //}

    map m = {};
    any a = m["a"];

    var cacheReturn = cache.get("Name");
    io:println(cacheReturn);
    //match cacheReturn {
      //  string val => io:println(val);
        //any|null err => io:println(err);
    //}
}
