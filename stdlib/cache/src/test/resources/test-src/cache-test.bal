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
    cache.put(key, value);
    return cache.size();
}

function testPutExistingEntry(string key, string value) returns [int, any|cache:Error] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put(key, "Random value");
    cache.put(key, value);
    return [cache.size(), cache.get(key)];
}

function testPutWithMaxAge(string key, string value, int maxAge) returns int {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put(key, value, maxAge);
    runtime:sleep(maxAge * 1000 * 2 + 1000);
    return cache.size();
}

function testGetExistingEntry(string key, string value) returns any|cache:Error {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put(key, value);
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
    cache.put(key, value, maxAgeInSeconds);
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
    cache.put(key, value);
    var result = cache.invalidate(key);
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
    cache.put(key1, value1);
    string key2 = "Ballerina";
    string value2 = "Language";
    cache.put(key2, value2);
    var result = cache.invalidateAll();
    return cache.size();
}

function testHasKey(string key, string value) returns boolean {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put(key, value);
    return cache.hasKey(key);
}

function testKeys(string key1, string value1, string key2, string value2) returns string[] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put(key1, value1);
    cache.put(key2, value2);
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
    cache.put(key1, value1);
    cache.put(key2, value2);
    return cache.size();
}

function testCacheEvictionWithCapacity1() returns [string[], int] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put("A", "1");
    cache.put("B", "2");
    cache.put("C", "3");
    cache.put("D", "4");
    cache.put("E", "5");
    cache.put("F", "6");
    cache.put("G", "7");
    cache.put("H", "8");
    cache.put("I", "9");
    cache.put("J", "10");
    cache.put("K", "11");
    return [cache.keys(), cache.size()];
}

function testCacheEvictionWithCapacity2() returns [string[], int] {
    cache:CacheConfig config = {
        capacity: 10,
        evictionFactor: 0.2
    };
    cache:Cache cache = new(config);
    cache.put("A", "1");
    cache.put("B", "2");
    cache.put("C", "3");
    cache.put("D", "4");
    cache.put("E", "5");
    cache.put("F", "6");
    cache.put("G", "7");
    cache.put("H", "8");
    cache.put("I", "9");
    cache.put("J", "10");
    var x = cache.get("A");
    cache.put("K", "11");
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
    cache.put("A", "1");
    cache.put("B", "2");
    cache.put("C", "3");
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
    cache.put("A", "1");
    cache.put("B", "2", 3600);
    cache.put("C", "3");
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
