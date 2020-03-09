## Module Overview

The `AbstractCache` object which has the common APIs as follows and there can be "custom implementations" with different data storage like file, database etc. with structural equivalent to the abstract object.

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

The `AbstractEvictionPolicy` object which has the common APIs as follows and there can be "custom implementations" with different eviction algorithms with structural equivalent to the abstract object. That custom implementation has to maintain the `LinkedList` data structure according to the eviction algorithm.

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

Ballerina Cache module provides a `Cache` object which is a map data structure based implementation of `AbstractCache` object. Also, it provides `LruEvictionPolicy` object which is based on LRU eviction algorithm.

While initializing the `Cache`, the developer have to pass the following parameters as the cache configurations.
- `capacity` - Max number of entries allowed for the cache
- `evictionPolicy` - The policy which defines the cache eviction algorithm
- `evictionFactor` - The factor which the entries will be evicted once the cache full
- `defaultMaxAgeInSeconds` - Freshness time of all the cache entries in seconds. This value can be overwritten by the `maxAgeInSeconds` property, when inserting entry to cache. '-1' means, the entries are valid forever.
- `cleanupIntervalInSeconds` - The interval time of the timer task which cleans the cache entries. This is an optional parameter.

For the better user experience the above config is initialized with default values as follows:

```ballerina
public type CacheConfig record {|
    int capacity = 100;
    AbstractEvictionPolicy evictionPolicy = new LruEvictionPolicy();
    float evictionFactor = 0.25;
    int defaultMaxAgeInSeconds = -1;
    int cleanupIntervalInSeconds?;
|};
```

There are 2 mandatory scenarios + 1 optional scenario where a cache entry get removed from the cache and maintain the freshness of the cache entries. The 2 independent factors which are eviction policy & freshness time of the cache  entry governs the 3 scenarios.

1. When using the `get` API, if the return cache entry has expired, it get removed.
2. When using the `put` API, if the cache size has reached its capacity, number of entries get removed based on the 'eviction policy' and 'eviction factor'.
3. If `cleanupIntervalInSeconds` (optional property) is configured, the timer task will remove the expired cache entries based on the configured interval.

The main benefit of using the `cleanupIntervalInSeconds` optional property is that the developer can optimize memory usage while adding some additional CPU costs and vice versa. The default behavior is the CPU optimized method.

The concept of the default `Cache` object is purely based on the Ballerina map data structure and a linked list data structure. The key of the map entry would be a string and the value of the map entry would be a node of the linked list.

```ballerina
public type Node record {|
    any value;
    Node? prev = ();
    Node? next = ();
|};
```

While using the cache, a `CacheEntry` record will be created and added as the value of the `Node` record. `Node` record will be inserted into the map data structure against the provided string key.

```ballerina
type CacheEntry record {|
    string key;
    any data;
    int expTime;
|};
```

The linked list data structure is purely used for the eviction of the cache. According to the user configured eviction policy, when inserting / updating / retrieving cache entries, the linked list data structure should get updated. Therefore, when eviction happens, cache entries can be removed efficiently, without iterating the complete map data structure.

**Example:** If the eviction policy is LRU, always the MRU item will be the head of the linked list. When eviction happens, nodes from the tail will be deleted without iterating the map.

Further, developers can implement a custom caching implementations based on different cache storage mechanisms (file, database etc.) and different eviction policies (MRU, FIFO etc.) as they wish. Ballerina provides a "map-based cache" as the default cache implementation.

### Samples

#### Cache Initialization

A basic sample cache of 100 capacity, which uses LRU as the eviction policy and eviction factor is 0.25 is as follows:
```ballerina
cache:Cache cache = new;
```

A basic sample cache of 1000 capacity, the eviction factor is 0.2, cache entry default freshness time as 1 hour, and clean up timer configured with 5 seconds interval  is as follows:
```ballerina
cache:Cache cache = new({
    capacity: 1000,
    evictionFactor: 0.2,
    defaultMaxAgeInSeconds: 3600
    cleanupIntervalInSeconds: 5
});
```

An advanced sample cache which uses a custom eviction policy along with the default capacity, eviction factor, max age and cleanup interval is as follows:
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

The simple way of using the initialized cache without handling errors is as follows:
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
    // implement what to do, if any error happen when inserting item to cache
}

any|cache:Error result = cache.get("key1");
if (result is cache:Error) {
    // implement what to do, if any error happen when retrieving item from the cache
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
