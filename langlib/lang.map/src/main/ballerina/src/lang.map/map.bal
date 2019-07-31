// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# A type parameter that is a subtype of `anydata|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type PureType anydata|error;

type MapIterator object {

    private map<Type> m;

    public function __init(map<Type> m) {
        self.m = m;
    }

    # Return the next member in map iterator, nil if end of iterator is reached.
    public function next() returns record {|
        Type value;
    |}? = external;
};

# Returns number of members in `m`.
#
# + m - the map
# + return - number of members in the map
public function length(map<any|error> m) returns int = external;

# Returns an iterator over the members of `m`.
#
# + m - the map
# + return - iterator object
public function iterator(map<Type> m) returns abstract object {
    public function next() returns record {|
        Type value;
    |}?;
} {
    MapIterator mapIterator = new(m);
    return mapIterator;
}

# Returns the member of map `m` with key `k`.
# Panics if `m` does not have a member with key `k`.
#
# + m - the map
# + k - the key
# + return - member matching key `k`
public function get(map<Type> m, string k) returns Type = external;

# Returns a map containing [key, member] pair as the value for each key.
#
# + m - the map
# + return - map of [key, member] pairs
public function entries(map<Type> m) returns map<[string, Type]> = external;

// Functional iteration

# Return a map with the result of applying function `func` to each member of map `m`.
#
# + m - the map
# + func - a function to apply to each member
# + return - new map containing result of applying function `func` to each member
public function 'map(map<Type> m, function(Type val) returns Type1 func) returns map<Type1> = external;

# Applies `func` to each member of `m`.
#
# + m - the map
# + func - a function to apply to each member
public function forEach(map<Type> m, function(Type val) returns () func) returns () = external;

# Returns a new map constructed from those elements of 'm' for which `func` returns true.
#
# + m - the map
# + func - a predicate to apply to each element to determine if it should be included
# + return - new map containig members which evaluate function 'func' to true
public function filter(map<Type> m, function(Type val) returns boolean func) returns map<Type> = external;

# Reduce operate on each member of `m` using combining function `func` to produce
# a new value combining all members of `m`.
#
# + m - the map
# + func - combining function
# + initial - initial value to first evaluation of combining function `func`
# + return - result of applying combining function to each member of the map
public function reduce(map<Type> m, function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 = external;

# Removes the member of `m` with key `k` and returns it.
# Panics if there is no such member.
#
# + m - the map
# + k - the key
# + return - removed member
public function remove(map<Type> m, string k) returns Type = external;

# Removes all members of `m`.
# Panics if any member cannot be removed.
#
# + m - the map
public function removeAll(map<any|error> m) returns () = external;

# Tells whether m has a member with key `k`.
#
# + m - the map
# + k - the key
# + return - true if m has a member with key `k`
public function hasKey(map<Type> m, string k) returns boolean = external;

# Returns a list of all the keys of map `m`.
#
# + m - the map
# + return - list of all keys
public function keys(map<any|error> m) returns string[] = external;
