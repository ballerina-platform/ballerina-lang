## Module Overview

This module provides APIs for handle caching in Ballerina. It consists of a default implementation based on map data structure. It also provides a default cache eviction policy object which is based on the LRU eviction algorithm.

The `cache:AbstractCache` object has the common APIs for the caching functionalities. "Custom implementations" of the cache can be done with different data storage like file, database, etc. with the structural equivalency to the `cache:AbstractCacheObject` object.

```ballerina
public type AbstractCache abstract object {
    public function put(string key, any value, int maxAgeInSeconds) returns Error?;
    public function get(string key) returns any|Error;
    public function invalidate(string key) returns Error?;
    public function invalidateAll() returns Error?;
    public function hasKey(string key) returns boolean;
    public function keys() returns string[];
    public function size() returns int;
    public function capacity() returns int;
};
```

The `cache:AbstractEvictionPolicy` object has the common APIs for the cache eviction functionalities. "Custom implementations" of the eviction policy can be done by maintaining the `cache:LinkedList` data structure according to the eviction algorithm.

```ballerina
public type AbstractEvictionPolicy abstract object {
    public function get(LinkedList list, Node node);
    public function put(LinkedList list, Node node);
    public function remove(LinkedList list, Node node);
    public function replace(LinkedList list, Node newNode, Node oldNode);
    public function clear(LinkedList list);
    public function evict(LinkedList list) returns Node?;
};
```

The Ballerina Cache module provides the `cache:Cache` object, which is a `map` data structure based implementation of the `cache:AbstractCache` object. It is not recommended to insert `()` as the value of the cache since it doesn't make any sense to cache a nil. Also, it provides the `cache:LruEvictionPolicy` object, which is based on the LRU eviction algorithm.

While initializing the `cache:Cache`, the developer has to pass the following parameters as the cache configurations.
- `capacity` - Maximum number of entries allowed for the cache
- `evictionPolicy` - The policy to define the cache eviction algorithm
- `evictionFactor` - The factor by which the entries will be evicted once the cache is full
- `defaultMaxAgeInSeconds` - Freshness time of all the cache entries in seconds. This value can be overwritten by the
`maxAgeInSeconds` property when inserting an entry to the cache. '-1' means the entries are valid forever.
- `cleanupIntervalInSeconds` - The interval time of the timer task which cleans the cache entries.
This is an optional parameter.

For a better user experience, the above mentioned configuration is initialized with default values as follows:

```ballerina
public type CacheConfig record {|
    int capacity = 100;
    AbstractEvictionPolicy evictionPolicy = new LruEvictionPolicy();
    float evictionFactor = 0.25;
    int defaultMaxAgeInSeconds = -1;
    int cleanupIntervalInSeconds?;
|};
```

There are 2 mandatory scenarios and 1 optional scenario, in which a cache entry gets removed from the cache and maintains the freshness of the cache entries. The 2 independent factors (i.e., eviction policy and freshness time of the cache entry) governs the 3 scenarios.

1. When using the `get` API, if the returning cache entry has expired, it gets removed.
2. When using the `put` API, if the cache size has reached its capacity, the number of entries get removed based on the
'eviction policy' and the 'eviction factor'.
3. If `cleanupIntervalInSeconds` (optional property) is configured, the timer task will remove the expired cache entries based on the configured interval.

The main benefit of using the `cleanupIntervalInSeconds` (optional) property is that the developer can optimize the memory usage while adding some additional CPU costs and vice versa. The default behaviour is the CPU-optimized method.

The concept of the default `cache:Cache` object is based on the Ballerina `map` data structure and the `cache:LinkedList` data structure. The key of the map entry would be a string and the value of the map entry would be a node of the linked list.

```ballerina
public type Node record {|
    any value;
    Node? prev = ();
    Node? next = ();
|};
```

While using the cache, a `cache:CacheEntry` record will be created and added as the value of the `cache:Node` record. The `cache:Node` record will be inserted into the map data structure with the provided string key.

```ballerina
type CacheEntry record {|
    string key;
    any data;
    int expTime;
|};
```

A linked list is used for the eviction of the cache. According to the user configured eviction policy, when inserting / updating / retrieving cache entries, the linked list will be updated. Therefore, when an eviction happens, cache entries can be removed efficiently without iterating the complete map data structure.

**Example:** If the eviction policy is LRU, always the MRU item will be the head of the linked list. When an eviction happens, nodes from the tail will be deleted without iterating the map.

Furthermore, the developers can implement custom caching implementations based on different cache storage mechanisms (file, database. etc.) and different eviction policies (MRU, FIFO, etc.). Ballerina provides a "map-based cache" as the default cache implementation.

### Samples

#### Cache Initialization

The following is a sample cache with a capacity of 100 entries, which uses LRU as the eviction policy and an eviction factor as 0.25, which are the default values:
```ballerina
cache:Cache cache = new;
```

The following is a sample with a cache with a capacity of 1000 entries, an eviction factor of 0.2, the default freshness time as 1 hour per an entry, and clean up timer configured with aa interval of 5 seconds.
```ballerina
cache:Cache cache = new({
    capacity: 1000,
    evictionFactor: 0.2,
    defaultMaxAgeInSeconds: 3600
    cleanupIntervalInSeconds: 5
});
```

The following is an example of an advanced cache, which uses a custom eviction policy along with the default capacity, eviction factor, max age, and cleanup interval.
```ballerina
public type CustomEvictionPolicy object {
    *cache:AbstractEvictionPolicy;
    public function get(LinkedList list, Node node) { // custom implementation }
    public function put(LinkedList list, Node node) { // custom implementation }
    public function remove(LinkedList list, Node node) { // custom implementation }
    public function replace(LinkedList list, Node newNode, Node oldNode) { // custom implementation }
    public function clear(LinkedList list) { // custom implementation }
    public function evict(LinkedList list) returns Node? { // custom implementation }
}

cache:Cache cache = new({
    capacity: 1000,
    evictionPolicy: new CustomEvictionPolicy()
});
```

#### Cache Usage

The simplest way of using the initialized cache without handling errors is as follows:
```ballerina
_ = check cache.put("key1", "value1");
string value = <string> check cache.get("key1");
_ = check cache.invalidate("key1");
_ = check cache.invalidateAll();
boolean hasKey = cache.hasKey("key1");
string[] keys = cache.keys();
int size = cache.size();
int capacity = cache.capacity();
```

The advanced way of using the initialized cache with error handling is as follows:
```ballerina
cache:Error? result = cache.put("key1", "value1");
if (result is cache:Error) {
    // Implement what to do if any error happens when inserting an item to the cache.
}

any|cache:Error result = cache.get("key1");
if (result is cache:Error) {
    // Implement what to do if any error happens when retrieving an item from the cache.
}
string value = <string>result;

cache:Error? result = check cache.invalidate("key1");
if (result is cache:Error) {
    // implement what to do, if any error happen when discarding item from the cache
}

cache:Error? result = check cache.invalidateAll();
if (result is cache:Error) {
    // implement what to do, if any error happen when discarding item from the cache
}

boolean hasKey = cache.hasKey("key1");

string[] keys = cache.keys();

int size = cache.size();

int capacity = cache.capacity();
```
