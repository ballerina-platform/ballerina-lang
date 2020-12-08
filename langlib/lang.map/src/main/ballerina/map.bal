// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type any|error;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type1 any|error;

# Returns number of members of a map.
#
# + m - the map
# + return - number of members in `m`
public isolated function length(map<any|error> m) returns int =@java:Method {
    'class: "org.ballerinalang.langlib.map.Length",
    name: "length"
} external;

# Returns an iterator over a map.
# The iterator will iterate over the members of the map not the keys.
# The `entries` function can be used to iterate over the keys and members together.
# The `keys` function can be used to iterator over just the keys.
#
# + m - the map
# + return - a new iterator object that will iterate over the members of `m`
public isolated function iterator(map<Type> m) returns object {
    public isolated function next() returns record {|
        Type value;
    |}?;
} {
    MapIterator mapIterator = new(m);
    return mapIterator;
}

# Returns the member of map `m` with key `k`.
# This for use in a case where it is known that the map has a specific key,
# and accordingly panics if `m` does not have a member with key `k`.
#
# + m - the map
# + k - the key
# + return - member with key `k`
public isolated function get(map<Type> m, string k) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.map.Get",
    name: "get"
} external;

# Returns a map containing [key, member] pair as the value for each key.
#
# + m - the map
# + return - a new map of [key, member] pairs
public isolated function entries(map<Type> m) returns map<[string, Type]> = @java:Method {
    'class: "org.ballerinalang.langlib.map.Entries",
    name: "entries"
} external;

// Functional iteration

# Applies a function each member of a map and returns a map of the result.
# The resulting map will have the same keys as the argument map.
#
# + m - the map
# + func - a function to apply to each member
# + return - new map containing result of applying parameter `func` to each member
public isolated function 'map(map<Type> m, @isolatedParam function(Type val) returns Type1 func) returns map<Type1> = @java:Method {
    'class: "org.ballerinalang.langlib.map.Map",
    name: "map"
} external;

# Applies a function to each member of a map.
# The parameter `func` is applied to each member of `m`.
#
# + m - the map
# + func - a function to apply to each member
public isolated function forEach(map<Type> m, @isolatedParam function(Type val) returns () func) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.map.ForEach",
    name: "forEach"
} external;

# Selects the members from a map for which a function returns true.
#
# + m - the map
# + func - a predicate to apply to each element to test whether it should be included
# + return - new map containing members for which `func` evaluates to true
public isolated function filter(map<Type> m, @isolatedParam function(Type val) returns boolean func) returns map<Type> = @java:Method {
    'class: "org.ballerinalang.langlib.map.Filter",
    name: "filter"
} external;

# Combines the members of a map using a combining function.
# The combining function takes the combined value so far and a member of the map,
# and returns a new combined value.
#
# + m - the map
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of `m` using `func`
public isolated function reduce(map<Type> m, @isolatedParam function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 =
@java:Method {
    'class: "org.ballerinalang.langlib.map.Reduce",
    name: "reduce"
} external;

# Removes a member of a map.
#
# + m - the map
# + k - the key
# + return - the member of `m` that had key `k`
# This removed the member of `m` with key `k` and returns it.
# It panics if there is no such member.
public isolated function remove(map<Type> m, string k) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.map.Remove",
    name: "remove"
} external;

# Removes a member of a map with a given key, if the map has member with the key.
#
# + m - the map
# + k - the key
# + return - the member of `m` that had key `k`, or `()` if `m` does not have a key `k`
# If `m` has a member with key `k`, it removes and returns it;
# otherwise it returns `()`.
public isolated function removeIfHasKey(map<Type> m, string k) returns Type? = @java:Method {
    'class: "org.ballerinalang.langlib.map.RemoveIfHasKey",
    name: "removeIfHasKey"
} external;

# Removes all members of a map.
# This panics if any member cannot be removed.
#
# + m - the map
public isolated function removeAll(map<any|error> m) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.map.RemoveAll",
    name: "removeAll"
} external;

# Tests whether m has a member with key `k`.
#
# + m - the map
# + k - the key
# + return - true if m has a member with key `k`
public isolated function hasKey(map<Type> m, string k) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.map.HasKey",
    name: "hasKey"
} external;

# Returns a list of all the keys of map `m`.
#
# + m - the map
# + return - a new list of all keys
public isolated function keys(map<any|error> m) returns string[] = @java:Method {
    'class: "org.ballerinalang.langlib.map.GetKeys",
    name: "keys"
} external;

# Returns a list of all the members of a map.
#
# + m - the map
# + return - an array whose members are the members of `m`
public isolated function toArray(map<Type> m) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.map.ToArray",
    name: "toArray"
} external;
