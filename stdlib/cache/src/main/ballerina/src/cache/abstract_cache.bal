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

# Represents Ballerina `AbstractCache` object and cache related operations. Any custom cache implementation should be
# object-wise similar.
public type AbstractCache abstract object {

    # Adds the given key, value pair to the cache.
    #
    # + key - Key of the cached value
    # + value - Value to be cached
    # + maxAgeInSeconds - The value in seconds which the cache entry is valid. '-1' means, the entry is valid forever.
    public function put(string key, any value, int maxAgeInSeconds);

    # Returns the cached value associated with the given key. If the provided cache key is not found,
    # () will be returned.
    #
    # + key - Key which is used to retrieve the cached value
    # + return - The cached value associated with the given key
    public function get(string key) returns any?;

    # Removes a cached value from the cache.
    #
    # + key - Key of the cache entry which needs to be removed
    public function remove(string key);

    # Remove all the cached values from the cache.
    public function removeAll();

    # Checks whether the given key has an associated cache value.
    #
    # + key - The key to be checked
    # + return - Whether the an associated cache value is available or not
    public function hasKey(string key) returns boolean;

    # Returns all keys from the cache.
    #
    # + return - Array of all keys from the cache
    public function keys() returns string[];

    # Returns the size of the cache.
    #
    # + return - The size of the cache
    public function size() returns int;

    # Returns the capacity of the cache.
    #
    # + return - The capacity of the cache
    public function capacity() returns int;

};
