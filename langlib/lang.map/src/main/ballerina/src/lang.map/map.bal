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
public function length(map<any|error> m) returns int = external;

# Returns an iterator over a map.
# The iterator will iterate over the members of the map not the keys.
# The `entries` function can be used to iterate over the keys and members together.
# The `keys` function can be used to iterator over just the keys.
#
# + m - the map
# + return - a new iterator object that will iterate over the members of `m`
public function iterator(map<Type> m) returns abstract object {
    public function next() returns record {|
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
public function get(map<Type> m, string k) returns Type = external;

# Returns a map containing [key, member] pair as the value for each key.
#
# + m - the map
# + return - a new map of [key, member] pairs
public function entries(map<Type> m) returns map<[string, Type]> = external;

// Functional iteration

# Applies a function each member of a map and returns a map of the result.
# The resulting map will have the same keys as the argument map.
#
# + m - the map
# + func - a function to apply to each member
# + return - new map containing result of applying function 'func' to each member
public function 'map(map<Type> m, function(Type val) returns Type1 func) returns map<Type1> = external;

# Applies a function to each member of a map.
# The function 'func' is applied to each member of `m`.
#
# + m - the map
# + func - a function to apply to each member
public function forEach(map<Type> m, function(Type val) returns () func) returns () = external;

# Selects the members from a map for which a function returns true.
#
# + m - the map
# + func - a predicate to apply to each element to test whether it should be included
# + return - new map containing members for which `func` evaluates to true
public function filter(map<Type> m, function(Type val) returns boolean func) returns map<Type> = external;

# Combines the members of a map using a combining function.
# The combining function takes the combined value so far and a member of the map,
# and returns a new combined value.
#
# + m - the map
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of `m` using `func`
public function reduce(map<Type> m, function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 = external;

# Removes a member of a map.
#
# + m - the map
# + k - the key
# + return - the member of `m` that had key `k`
# This removed the member of `m` with key `k` and returns it.
# It panics if there is no such member.
public function remove(map<Type> m, string k) returns Type = external;

# Removes all members of a map.
# This panics if any member cannot be removed.
#
# + m - the map
public function removeAll(map<any|error> m) returns () = external;

# Tests whether m has a member with key `k`.
#
# + m - the map
# + k - the key
# + return - true if m has a member with key `k`
public function hasKey(map<Type> m, string k) returns boolean = external;

# Returns a list of all the keys of map `m`.
#
# + m - the map
# + return - a new list of all keys
public function keys(map<any|error> m) returns string[] = external;
