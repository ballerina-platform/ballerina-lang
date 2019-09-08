## Module Overview
This module offers a cache that supports time-based eviction, and size-based eviction. This means that entries are 
removed from the cache once the expiry time has elapsed (i.e., time-based eviction), or when the cache runs out of 
space (i.e., size-based eviction). Caching is useful when a value takes significant cost or time to compute and 
retrieve, and it is therefore useful to cache it for future use.

The following code snippet creates a cache that can hold a maximum of 10 entries. An entry of this cache expires 
one minute after its last access. When the cache is full, 20% of the entries (i.e., 2 entries) will be evicted to make 
space for new entries.

```ballerina
// The `evictionFactor` specifies the percentage of items
// that will be evicted when the cache is full.
cache:Cache cache = new(expiryTimeInMillis = 60000, capacity = 10, evictionFactor = 0.2);

// Enter a value, and a key to reference that value.
// This will be a new entry in the cache.
cache.put("key-a", "value-a");

// Use the same key to retrieve the value.
string returnValue = <string>cache.get("key-a");
```

