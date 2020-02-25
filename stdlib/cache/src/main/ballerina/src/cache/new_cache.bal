// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/task;
import ballerina/time;

# The `LRU` eviction algorithm.
public const LRU = "Least Recently Used";
# The `FIFO` eviction algorithm.
public const FIFO = "First In First Out";

# The collection of eviction algorithms.
public type EvictionPolicy LRU|FIFO;

// TODO: Rename the record into `CacheConfig`
# Represents cache configuration.
#
# + capacity - Maximum number of entries allowed
# + evictionPolicy - The policy which defines the cache eviction algorithm
# + evictionFactor - The factor which the entries will be evicted once the cache full
# + defaultMaxAgeInSeconds - The default value in seconds which all the cache entries are valid.
#                            '-1' means, the entries are valid forever.
#                            This will be overwritten by the the `maxAge` property set when inserting item to cache
# + timerIntervalInSeconds - Interval of the timer task which clean up the cache
public type NewCacheConfig record {|
    int capacity;
    EvictionPolicy evictionPolicy;
    float evictionFactor = 0.25;
    int defaultMaxAgeInSeconds = -1;
    int timerIntervalInSeconds?;
|};

// TODO: Rename the record into `CacheEntry`
type NewCacheEntry record {|
    string key;
    any data;
    int expTime;       // exp time since epoch. calculated based on the `maxAge` parameter when inserting to map
|};

// TODO: Remove by fixing https://github.com/ballerina-platform/ballerina-lang/issues/21268
type MapAndList record {|
    map<Node> entries;
    LinkedList list;
|};

// Cleanup service which cleans the cache entries periodically.
service cleanupService = service {
    resource function onTrigger(MapAndList mapAndList) {
        cleanup(mapAndList);
    }
};

// TODO: Rename the object into `Cache`
# Represents Ballerina `Cache` object and cache related operations.
public type NewCache object {

    *AbstractCache;

    private int capacity;
    private EvictionPolicy evictionPolicy;
    private float evictionFactor;
    private int defaultMaxAgeInSeconds;
    private map<Node> entries = {};
    private LinkedList list;

    # Creates a new `Cache` object.
    #
    # + cacheConfig - Cache configurations
    public function __init(NewCacheConfig cacheConfig) {
        self.capacity = cacheConfig.capacity;
        self.evictionPolicy = cacheConfig.evictionPolicy;
        self.evictionFactor = cacheConfig.evictionFactor;
        self.defaultMaxAgeInSeconds = cacheConfig.defaultMaxAgeInSeconds;

        // Cache capacity must be a positive value.
        if (self.capacity <= 0) {
            panic error(CACHE_ERROR, message = "Capacity must be greater than 0.");
        }
        // Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).
        if (self.evictionFactor <= 0 || self.evictionFactor > 1) {
            panic error(CACHE_ERROR, message = "Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).");
        }

        // Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).
        if (self.defaultMaxAgeInSeconds != -1 && self.defaultMaxAgeInSeconds < 0) {
            panic error(CACHE_ERROR, message = "Default max age should be greated than 0.");
        }

        self.list = {
            head: (),
            tail: ()
        };
        int? timerIntervalInSeconds = cacheConfig?.timerIntervalInSeconds;
        if (timerIntervalInSeconds is int) {
            task:TimerConfiguration timerConfiguration = {
                intervalInMillis: timerIntervalInSeconds,
                initialDelayInMillis: timerIntervalInSeconds
            };
            task:Scheduler cleanupScheduler = new(timerConfiguration);
            MapAndList mapAndList = {
                entries: self.entries,
                list: self.list
            };
            task:SchedulerError? result = cleanupScheduler.attach(cleanupService, attachment = mapAndList);
            if (result is task:SchedulerError) {
                record {| string message?; anydata|error...; |} detail = result.detail();
                panic error(CACHE_ERROR, message = "Failed to create the cache cleanup task: " + <string>detail["message"]);
            }
            result = cleanupScheduler.start();
            if (result is task:SchedulerError) {
                record {| string message?; anydata|error...; |} detail = result.detail();
                panic error(CACHE_ERROR, message = "Failed to start the cache cleanup task: " + <string>detail["message"]);
            }
        }
    }

    # Adds the given key, value pair to the cache.
    #
    # + key - Key of the cached value
    # + value - Value to be cached
    # + maxAgeInSeconds - The value in seconds which the cache entry is valid. '-1' means, the entry is valid forever.
    public function put(string key, any value, int maxAgeInSeconds = -1) {
        lock {
            // If the current cache is full (i.e. size = capacity), evict cache.
            if (self.size() == self.capacity) {
                evict(self.entries, self.list, self.evictionPolicy, self.capacity, self.evictionFactor);
            }

            // Calculate the `expTime` of the cache entry based on the `maxAgeInSeconds` property and
            // `defaultMaxAgeInSeconds` property.
            int calculatedExpTime = -1;
            if (maxAgeInSeconds != -1 && maxAgeInSeconds > 0) {
                calculatedExpTime = time:nanoTime() + (maxAgeInSeconds * 1000 * 1000 * 1000);
            } else {
                if (self.defaultMaxAgeInSeconds != -1) {
                    calculatedExpTime = time:nanoTime() + (self.defaultMaxAgeInSeconds * 1000 * 1000 * 1000);
                }
            }

            NewCacheEntry entry = {
                key: key,
                data: value,
                expTime: calculatedExpTime
            };

            if (self.hasKey(key)) {
                Node node = self.entries.get(key);
                putOnEvictionPolicy(self.evictionPolicy, self.list, node, true);
                return;
            }
            Node newNode = { value: entry };
            putOnEvictionPolicy(self.evictionPolicy, self.list, newNode, false);
            self.entries[key] = newNode;
        }
    }

    # Returns the cached value associated with the given key. If the provided cache key is not found,
    # () will be returned.
    #
    # + key - Key which is used to retrieve the cached value
    # + return - The cached value associated with the given key
    public function get(string key) returns any? {
        lock {
            if (!self.hasKey(key)) {
                return;
            }

            Node node = self.entries.get(key);
            NewCacheEntry entry = <NewCacheEntry>node.value;

            // Check whether the cache entry is already expired. Even though the cache cleaning task is configured
            // and runs in predefined intervals, sometimes the cache entry might not have been removed at this point
            // even though it is expired. So this check guarantees that the expired cache entries will not be returned.
            if (entry.expTime != -1 && entry.expTime < time:nanoTime()) {
                remove(self.list, node);
                removeEntry(self.entries, key);
                return;
            }

            getOnEvictionPolicy(self.evictionPolicy, self.list, node);
            return entry.data;
        }
    }

    # Removes a cached value from the cache.
    #
    # + key - Key of the cache entry which needs to be removed
    public function remove(string key) {
        lock {
            if (!self.hasKey(key)) {
                return;
            }

            Node node = self.entries.get(key);
            remove(self.list, node);
            removeEntry(self.entries, key);
        }
    }

    # Remove all the cached values from the cache.
    public function removeAll() {
        lock {
            clear(self.list);
            var result = trap self.entries.removeAll();
            // The return result (removed entry or the error which occurred due to unavailability of the key)
            // is ignored since no purpose of handling it.
        }
    }

    # Checks whether the given key has an associated cache value.
    #
    # + key - The key to be checked
    # + return - Whether the an associated cache value is available or not
    public function hasKey(string key) returns boolean {
        return self.entries.hasKey(key);
    }

    # Returns all keys from the cache.
    #
    # + return - Array of all keys from the cache
    public function keys() returns string[] {
        return self.entries.keys();
    }

    # Returns the size of the cache.
    #
    # + return - The size of the cache
    public function size() returns int {
        return self.entries.length();
    }

    # Returns the capacity of the cache.
    #
    # + return - The capacity of the cache
    public function capacity() returns int {
        return self.capacity;
    }
};

function evict(map<Node> entries, LinkedList list, EvictionPolicy evictionPolicy, int capacity, float evictionFactor) {
    int evictionKeysCount = <int>(capacity * evictionFactor);
    match (evictionPolicy) {
        LRU|FIFO => {
            foreach int i in 1...evictionKeysCount {
                Node? tail = removeLast(list);
                if (tail is Node) {
                    NewCacheEntry entry = <NewCacheEntry>tail.value;
                    removeEntry(entries, entry.key);
                } else {
                    break;
                }
            }
        }
    }
}

function putOnEvictionPolicy(EvictionPolicy evictionPolicy, LinkedList list, Node node, boolean alreadyAvailable) {
    match (evictionPolicy) {
        LRU => {
            if (alreadyAvailable) {
                remove(list, node);
                addFirst(list, node);
                return;
            }
            addFirst(list, node);
        }
        FIFO => {
            addFirst(list, node);
        }
    }
}

function getOnEvictionPolicy(EvictionPolicy evictionPolicy, LinkedList list, Node node) {
    match (evictionPolicy) {
        LRU => {
            remove(list, node);
            addFirst(list, node);
        }
        FIFO => {
            return;
        }
    }
}

function cleanup(MapAndList mapAndList) {
    foreach Node node in mapAndList.entries {
        NewCacheEntry entry = <NewCacheEntry>node.value;
        if (entry.expTime != -1 && entry.expTime < time:nanoTime()) {
            remove(mapAndList.list, node);
            removeEntry(mapAndList.entries, entry.key);
            return;
        }
    }
}

function removeEntry(map<Node> entries, string key) {
    var result = trap entries.remove(key);
    // The return result (removed entry or the error which occurred due to unavailability of the key)
    // is ignored since no purpose of handling it.
}
