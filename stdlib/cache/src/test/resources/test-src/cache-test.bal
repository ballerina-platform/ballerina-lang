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

import ballerina/cache;
import ballerina/runtime;

function testCreateCache() returns int {
    cache:LruEvictionPolicy lruEvictionPolicy = new;
    cache:CacheConfig config = {
        capacity: 10,
        evictionPolicy: lruEvictionPolicy,
        evictionFactor: 0.2,
        defaultMaxAgeInSeconds: 3600,
        cleanupIntervalInSeconds: 5
    };
    cache:Cache cache = new(config);
    return cache.size();
}

function testPutNewEntry(string key, string value) returns int {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key, value);
    return cache.size();
}

function testPutExistingEntry(string key, string value) returns [int, any|cache:Error] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key, "Random value");
    checkpanic cache.put(key, value);
    return [cache.size(), cache.get(key)];
}

function testPutWithMaxAge(string key, string value, int maxAge) returns int {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key, value, maxAge);
    runtime:sleep(maxAge * 1000 * 2 + 1000);
    return cache.size();
}

function testGetExistingEntry(string key, string value) returns any|cache:Error {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key, value);
    return cache.get(key);
}

function testGetNonExistingEntry(string key) returns any|cache:Error {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    return cache.get(key);
}

function testGetExpiredEntry(string key, string value) returns any|cache:Error {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    int maxAgeInSeconds = 1;
    checkpanic cache.put(key, value, maxAgeInSeconds);
    runtime:sleep(maxAgeInSeconds * 1000 * 2 + 1000);
    return cache.get(key);
}

function testRemove() returns int {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    string key = "Hello";
    string value = "Ballerina";
    checkpanic cache.put(key, value);
    checkpanic cache.invalidate(key);
    return cache.size();
}

function testRemoveAll() returns int {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    string key1 = "Hello";
    string value1 = "Ballerina";
    checkpanic cache.put(key1, value1);
    string key2 = "Ballerina";
    string value2 = "Language";
    checkpanic cache.put(key2, value2);
    checkpanic cache.invalidateAll();
    return cache.size();
}

function testHasKey(string key, string value) returns boolean {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key, value);
    return cache.hasKey(key);
}

function testKeys(string key1, string value1, string key2, string value2) returns string[] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key1, value1);
    checkpanic cache.put(key2, value2);
    return cache.keys();
}

function testCapacity(int capacity) returns int {
    cache:CacheConfig config = {
        capacity: capacity,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    return cache.capacity();
}

function testSize(string key1, string value1, string key2, string value2) returns int {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put(key1, value1);
    checkpanic cache.put(key2, value2);
    return cache.size();
}

function testCacheEvictionWithCapacity1() returns [string[], int] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put("A", "1");
    checkpanic cache.put("B", "2");
    checkpanic cache.put("C", "3");
    checkpanic cache.put("D", "4");
    checkpanic cache.put("E", "5");
    checkpanic cache.put("F", "6");
    checkpanic cache.put("G", "7");
    checkpanic cache.put("H", "8");
    checkpanic cache.put("I", "9");
    checkpanic cache.put("J", "10");
    checkpanic cache.put("K", "11");
    return [cache.keys(), cache.size()];
}

function testCacheEvictionWithCapacity2() returns [string[], int] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    checkpanic cache.put("A", "1");
    checkpanic cache.put("B", "2");
    checkpanic cache.put("C", "3");
    checkpanic cache.put("D", "4");
    checkpanic cache.put("E", "5");
    checkpanic cache.put("F", "6");
    checkpanic cache.put("G", "7");
    checkpanic cache.put("H", "8");
    checkpanic cache.put("I", "9");
    checkpanic cache.put("J", "10");
    any|cache:Error x = cache.get("A");
    checkpanic cache.put("K", "11");
    return [cache.keys(), cache.size()];
}

function testCacheEvictionWithTimer1() returns [string[], int] {
    int cleanupIntervalInSeconds = 2;
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2,
        defaultMaxAgeInSeconds: 1,
        cleanupIntervalInSeconds: cleanupIntervalInSeconds
    };
    cache:Cache cache = new(config);
    checkpanic cache.put("A", "1");
    checkpanic cache.put("B", "2");
    checkpanic cache.put("C", "3");
    runtime:sleep(cleanupIntervalInSeconds * 1000 * 2 + 1000);
    return [cache.keys(), cache.size()];
}

function testCacheEvictionWithTimer2() returns [string[], int] {
    int cleanupIntervalInSeconds = 2;
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2,
        defaultMaxAgeInSeconds: 1,
        cleanupIntervalInSeconds: cleanupIntervalInSeconds
    };
    cache:Cache cache = new(config);
    checkpanic cache.put("A", "1");
    checkpanic cache.put("B", "2", 3600);
    checkpanic cache.put("C", "3");
    runtime:sleep(cleanupIntervalInSeconds * 1000 * 2 + 1000);
    return [cache.keys(), cache.size()];
}

function testCreateCacheWithZeroCapacity() {
    cache:CacheConfig config = {
        capacity: 0,
        evictionFactor: 0.2
    };
    cache:Cache c = new(config);
}

function testCreateCacheWithNegativeCapacity() {
    cache:CacheConfig config = {
        capacity: -1,
        evictionFactor: 0.2
    };
    cache:Cache c = new(config);
}

function testCreateCacheWithZeroEvictionFactor() {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0
    };
    cache:Cache c = new(config);
}

function testCreateCacheWithNegativeEvictionFactor() {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: -1
    };
    cache:Cache c = new(config);
}

function testCreateCacheWithInvalidEvictionFactor() {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 1.1
    };
    cache:Cache c = new(config);
}

function testCreateCacheWithZeroDefaultMaxAge() {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2,
        defaultMaxAgeInSeconds: 0
    };
    cache:Cache c = new(config);
}

function testCreateCacheWithNegativeDefaultMaxAge() {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2,
        defaultMaxAgeInSeconds: -10
    };
    cache:Cache c = new(config);
}
