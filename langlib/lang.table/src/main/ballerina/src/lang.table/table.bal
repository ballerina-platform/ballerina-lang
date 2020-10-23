// Copyright (c) 2018, 2019, 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# A type parameter that is a subtype of `anydata`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type KeyType anydata;

# Returns number of members of a table.
#
# + t - the table
# + return - number of members in `t`
public isolated function length(table<any|error> t) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.table.Length",
    name: "length"
} external;

# Returns an iterator over a table.
# The iterator will iterate over the members of the table not the keys.
# The `entries` function can be used to iterate over the keys and members together.
# The `keys` function can be used to iterator over just the keys.
#
# + t - the table
# + return - a new iterator object that will iterate over the members of `t`
public isolated function iterator(table<Type> t) returns object {
    public isolated function next() returns record {|
        Type value;
    |}?;
} {
    TableIterator tableIterator = new(t);
    return tableIterator;
   }

# Returns the member of table `t` with key `k`.
# This for use in a case where it is known that the table has a specific key,
# and accordingly panics if `t` does not have a member with key `k`.
#
# + t - the table
# + k - the key
# + return - member with key `k`
public isolated function get(table<Type> key<KeyType> t, KeyType k) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.table.Get",
    name: "get"
} external;

# Adds a member `val` to table `t`, replacing any member with the same key value.
# If `val` replaces an existing member, it will have the same position
# in the order of the members as the existing member;
# otherwise, it will be added as the last member.
# It panics if `val` is inconsistent with the inherent type of `t`.
#
# + t - the table
# + val - the member
public isolated function put(table<Type> t, Type val) = @java:Method {
    'class: "org.ballerinalang.langlib.table.Put",
    name: "put"
} external;

# Adds a member `val` to table `t`.
# It will be added as the last member.
# It panics if `val` has the same key as an existing member of `t`,
# or if `val` is inconsistent with the inherent type of `t`.
#
# + t - the table
# + val - the member
public isolated function add(table<Type> t, Type val) = @java:Method {
    'class: "org.ballerinalang.langlib.table.Add",
    name: "add"
} external;

// Functional iteration

# Applies a function each member of a table and returns a table of the result.
#
# + t - the table
# + func - a function to apply to each member
# + return - new table containing result of applying `func` to each member
public isolated function 'map(table<Type> t, @isolatedParam function(Type val) returns Type1 func)
   returns table<Type1> key<never> = @java:Method {
    'class: "org.ballerinalang.langlib.table.Map",
    name: "map"
} external;

# Applies a function to each member of a table.
# The `func` is applied to each member of `t`.
#
# + t - the table
# + func - a function to apply to each member
public isolated function forEach(table<Type> t, @isolatedParam function(Type val) returns () func) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.table.Foreach",
    name: "forEach"
} external;

# Selects the members from a table for which a function returns true.
#
# + t - the table
# + func - a predicate to apply to each member to test whether it should be included
# + return - new table containing members for which `func` evaluates to true
public isolated function filter(table<Type> key<KeyType> t, @isolatedParam function(Type val) returns boolean func)
   returns table<Type> key<KeyType> = @java:Method {
    'class: "org.ballerinalang.langlib.table.Filter",
    name: "filter"
} external;

# Combines the members of a table using a combining function.
# The combining function takes the combined value so far and a member of the table,
# and returns a new combined value.
#
# + t - the table
# + func - combining function
# + initial - initial value for the first argument of combining `func`
# + return - result of combining the members of `t` using `func`
public isolated function reduce(table<Type> t, @isolatedParam function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 =
@java:Method {
    'class: "org.ballerinalang.langlib.table.Reduce",
    name: "reduce"
} external;

# Removes a member of a table.
#
# + t - the table
# + k - the key
# + return - the member of `t` that had key `k`
# This removed the member of `t` with key `k` and returns it.
# It panics if there is no such member.
public isolated function remove(table<Type> key<KeyType> t, KeyType k) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.table.Remove",
    name: "remove"
} external;

# Removes a member of a table with a given key, if the table has member with the key.
#
# + t - the table
# + k - the key
# + return - the member of `t` that had key `k`, or `()` if `t` does not have a key `k`
# If `t` has a member with key `k`, it removes and returns it;
# otherwise it returns `()`.
public isolated function removeIfHasKey(table<Type> key<KeyType> t, KeyType k) returns Type? = @java:Method {
    'class: "org.ballerinalang.langlib.table.RemoveIfHasKey",
    name: "removeIfHasKey"
} external;

# Removes all members of a table.
# This panics if any member cannot be removed.
#
# + t - the table
public isolated function removeAll(table<any|error> t) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.table.RemoveAll",
    name: "removeAll"
} external;

# Tests whether `t` has a member with key `k`.
#
# + t - the table
# + k - the key
# + return - true if `t` has a member with key `k`
public isolated function hasKey(table<Type> key<KeyType> t, KeyType k) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.table.HasKey",
    name: "hasKey"
} external;

# Returns a list of all the keys of table `t`.
#
# + t - the table
# + return - a new list of all keys
public isolated function keys(table<any|error> key<KeyType> t) returns KeyType[] = @java:Method {
    'class: "org.ballerinalang.langlib.table.GetKeys",
    name: "keys"
} external;

# Returns a list of all the members of a table.
#
# + t - the table
# + return - an array whose members are the members of `t`
public isolated function toArray(table<Type> t) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.table.ToArray",
    name: "toArray"
} external;

# Returns the next available integer key.
# + t - the table with a key of type int
# + return - an integer not yet used as a key
# This is maximum used key value + 1, or 0 if no key used
# XXX should it be 0, if the maximum used key value is < 0?
# Provides similar functionality to auto-increment
public isolated function nextKey(table<any|error> key<int> t) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.table.NextKey",
    name: "nextKey"
} external;
