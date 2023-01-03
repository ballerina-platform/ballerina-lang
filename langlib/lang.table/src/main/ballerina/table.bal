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

import ballerina/jballerina.java;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type MapType map<any|error>;

# A type parameter that is a subtype of `map<any|error>`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type MapType1 map<any|error>;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type any|error;

# A type parameter that is a subtype of `anydata`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type KeyType anydata;

# Returns number of members of a table.
#
# ```ballerina
# table<record {|string name;|}> students = table [
#     {name: "Jo"},
#     {name: "Smith"}
# ];
# students.length() ⇒ 2
# ```
#
# + t - the table
# + return - number of members in parameter `t`
public isolated function length(table<map<any|error>> t) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.table.Length",
    name: "length"
} external;

# Returns an iterator over a table.
#
# The iterator will iterate over the members of the table not the keys.
# The `entries` function can be used to iterate over the keys and members together.
# The `keys` function can be used to iterator over just the keys.
#
# ```ballerina
# object {
#     public isolated function next() returns record {|record {|string name;|} value;|}?;
# } iterator = table [
#     {name: "Jo"},
#     {name: "Smith"}
# ].iterator();
# iterator.next() ⇒ {"value":{"name":"Jo"}}
# ```
#
# + t - the table
# + return - a new iterator object that will iterate over the members of parameter `t`
public isolated function iterator(table<MapType> t) returns object {
    public isolated function next() returns record {|
        MapType value;
    |}?;
} {
    TableIterator tableIterator = new(t);
    return tableIterator;
   }

# Returns the member of an table with a particular key.
#
# This for use in a case where it is known that the table has a specific key,
# and accordingly panics if parameter `t` does not have a member with key parameter `k`.
#
# ```ballerina
# table<record {|readonly string index; string name;|}> key(index) students = table [
#     {index: "220001CS", name: "Jo"},
#     {index: "220002CS", name: "Sam"}
# ];
# students.get("220002CS") ⇒ {"index":"220002CS","name":"Sam"}
#
# students.get("110002CS") ⇒ panic
# ```
#
# + t - the table
# + k - the key
# + return - member with key parameter `k`
public isolated function get(table<MapType> key<KeyType> t, KeyType k) returns MapType = @java:Method {
    'class: "org.ballerinalang.langlib.table.Get",
    name: "get"
} external;

# Adds a member to a table value, replacing any member with the same key value.
#
# If parameter `val` replaces an existing member, it will have the same position
# in the order of the members as the existing member;
# otherwise, it will be added as the last member.
# It panics if parameter `val` is inconsistent with the inherent type of parameter `t`.
#
# ```ballerina
# table<record {|int id; string name;|}> employees = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# employees.put({id: 1, name: "Pat"});
# employees ⇒ [{"id":1,"name":"Jo"},{"id":2,"name":"Sam"},{"id":1,"name":"Pat"}]
#
# table<record {|readonly int id; string name;|}> key(id) students = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# 
# students.put({id: 3, name: "Pat"});
# students ⇒ [{"id":1,"name":"Jo"},{"id":2,"name":"Sam"},{"id":3,"name":"Pat"}]
#
# students.put({id: 1, name: "Kane"});
# students ⇒ [{"id":1,"name":"Kane"},{"id":2,"name":"Sam"},{"id":3,"name":"Pat"}]
#
# table<record {readonly int id;}> key(id) studentIds = students;
# studentIds.put({id: 7}) ⇒ panic
# ```
#
# + t - the table
# + val - the member
public isolated function put(table<MapType> t, MapType val) = @java:Method {
    'class: "org.ballerinalang.langlib.table.Put",
    name: "put"
} external;

# Adds a member to a table.
#
# It will be added as the last member.
# It panics if parameter `val` has the same key as an existing member of parameter `t`,
# or if parameter `val` is inconsistent with the inherent type of `t`.
#
# ```ballerina
# table<record {|int id; string name;|}> employees = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# employees.add({id: 1, name: "Pat"});
# employees ⇒ [{"id":1,"name":"Jo"},{"id":2,"name":"Sam"},{"id":1,"name":"Pat"}]
#
# table<record {|readonly int id; string name;|}> key(id) students = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# 
# students.add({id: 3, name: "Pat"});
# students ⇒ [{"id":1,"name":"Jo"},{"id":2,"name":"Sam"},{"id":3,"name":"Pat"}]
#
# students.add({id: 1, name: "James"}) ⇒ panic
#
# table<record {readonly int id;}> key(id) studentIds = students;
# studentIds.add({id: 7}) ⇒ panic
# ```
#
# + t - the table
# + val - the member
public isolated function add(table<MapType> t, MapType val) = @java:Method {
    'class: "org.ballerinalang.langlib.table.Add",
    name: "add"
} external;

// Functional iteration

# Applies a function to each member of a table and returns a table of the result.
#
# ```ballerina
# table<record {|int id; int math; int physics;|}> students = table [
#     {id: 1, math: 78, physics: 70},
#     {id: 2, math: 83, physics: 80}
# ];
# students.map(student => {id: student.id, avg: (student.math + student.physics) / 2}) ⇒ [{"id":1,"avg":74},{"id":2,"avg":81}]
# ```
#
# + t - the table
# + func - a function to apply to each member
# + return - new table containing result of applying parameter `func` to each member
public isolated function 'map(table<MapType> t, @isolatedParam function(MapType val) returns MapType1 func)
   returns table<MapType1> key<never> = @java:Method {
    'class: "org.ballerinalang.langlib.table.Map",
    name: "map"
} external;

# Applies a function to each member of a table.
#
# The parameter `func` is applied to each member of parameter `t`.
#
# ```ballerina
# table<record {|string name; int salary;|}> employees = table [
#     {name: "Jo", salary: 1200},
#     {name: "Emma", salary: 800}
# ];
# int total = 0;
# employees.forEach(function(record {|string name; int salary;|} emp) {
#     total += emp.salary;
# });
# total ⇒ 2000
# ```
#
# + t - the table
# + func - a function to apply to each member
public isolated function forEach(table<MapType> t, @isolatedParam function(MapType val) returns () func) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.table.Foreach",
    name: "forEach"
} external;

# Selects the members from a table for which a function returns true.
#
# The resulting table will have the same keys as the argument table.
#
# ```ballerina
# table<record {|int id; int salary;|}> employees = table [
#     {id: 1, salary: 1200},
#     {id: 2, salary: 1100},
#     {id: 3, salary: 800}
# ];
# employees.filter(emp => emp.salary < 1000) ⇒ [{"id":3,"salary":800}]
# ```
#
# + t - the table
# + func - a predicate to apply to each member to test whether it should be included
# + return - new table containing members for which parameter `func` evaluates to true
public isolated function filter(table<MapType> key<KeyType> t, @isolatedParam function(MapType val) returns boolean func)
   returns table<MapType> key<KeyType> = @java:Method {
    'class: "org.ballerinalang.langlib.table.Filter",
    name: "filter"
} external;

# Combines the members of a table using a combining function.
#
# The combining function takes the combined value so far and a member of the table,
# and returns a new combined value.
#
# ```ballerina
# table<record {int id; int salary;}> employees = table [
#     {id: 1, salary: 1200},
#     {id: 2, salary: 1100},
#     {id: 3, salary: 800}
# ];
# employees.reduce(isolated function (int total, record {int id; int salary;} next) returns int => total + next.salary, 0) ⇒ 3100
# ```
#
# + t - the table
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of parameter `t` using parameter `func`
public isolated function reduce(table<MapType> t, @isolatedParam function(Type accum, MapType val) returns Type func, Type initial) returns Type =
@java:Method {
    'class: "org.ballerinalang.langlib.table.Reduce",
    name: "reduce"
} external;

# Removes a member of a table.
#
# This removed the member of parameter `t` with key parameter `k` and returns it.
# It panics if there is no such member.
#
# ```ballerina
# table<record {|readonly int id; string name;|}> key(id) students = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# students.remove(1) ⇒ {"id":1,"name":"Jo"}
# students ⇒ [{"id":2,"name":"Sam"}]
#
# students.remove(5) ⇒ panic
# ```
#
# + t - the table
# + k - the key
# + return - the member of parameter `t` that had key parameter `k`
public isolated function remove(table<MapType> key<KeyType> t, KeyType k) returns MapType = @java:Method {
    'class: "org.ballerinalang.langlib.table.Remove",
    name: "remove"
} external;

# Removes a member of a table with a given key, if the table has member with the key.
#
# If parameter `t` has a member with key parameter `k`, it removes and returns it;
# otherwise it returns `()`.
#
# ```ballerina
# table<record {|readonly int id; string name;|}> key(id) students = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# 
# students.removeIfHasKey(1) ⇒ {"id":1,"name":"Jo"}
# students ⇒ [{"id":2,"name":"Sam"}]
# 
# students.removeIfHasKey(3) is () ⇒ true
# ```
#
# + t - the table
# + k - the key
# + return - the member of parameter `t` that had key parameter `k`, or `()` if parameter `t` does not have a key parameter `k`
public isolated function removeIfHasKey(table<MapType> key<KeyType> t, KeyType k) returns MapType? = @java:Method {
    'class: "org.ballerinalang.langlib.table.RemoveIfHasKey",
    name: "removeIfHasKey"
} external;

# Removes all members of a table.
#
# This panics if any member cannot be removed.
#
# ```ballerina
# table<record {|string name;|}> students = table [
#     {name: "Jo"},
#     {name: "Sam"}
# ];
# students.removeAll() is () ⇒ true
# students ⇒ []
#
# table<record {|int score;|}> scores = <readonly> table [
#     {score: 30},
#     {score: 40}
# ];
# scores.removeAll() ⇒ panic
# ```
#
# + t - the table
public isolated function removeAll(table<map<any|error>> t) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.table.RemoveAll",
    name: "removeAll"
} external;

# Tests whether a table has a member with a particular key.
#
# ```ballerina
# table<record {|readonly int id; string name;|}> key(id) students = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# 
# students.hasKey(1) ⇒ true
# students.hasKey(5) ⇒ false
# ```
#
# + t - the table
# + k - the key
# + return - true if parameter `t` has a member with key parameter `k`
public isolated function hasKey(table<MapType> key<KeyType> t, KeyType k) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.table.HasKey",
    name: "hasKey"
} external;

# Returns a list of all the keys of a table.
#
# ```ballerina
# table<record {|readonly string code; string name;|}> key(code) countries = table [
#     {code: "CAN", name: "Canada"},
#     {code: "DNK", name: "Denmark"},
#     {code: "NPL", name: "Nepal"}
# ];
# countries.keys() ⇒ ["CAN","DNK","NPL"]
# ```
#
# + t - the table
# + return - a new list of all keys
public isolated function keys(table<map<any|error>> key<KeyType> t) returns KeyType[] = @java:Method {
    'class: "org.ballerinalang.langlib.table.GetKeys",
    name: "keys"
} external;

# Returns a list of all the members of a table.
#
# ```ballerina
# table [
#     {code: "CAN", name: "Canada"},
#     {code: "DNK", name: "Denmark"},
#     {code: "NPL", name: "Nepal"}
# ].toArray() ⇒ [{"code":"CAN","name":"Canada"},{"code":"DNK","name":"Denmark"},{"code":"NPL","name":"Nepal"}]
# ```
#
# + t - the table
# + return - an array whose members are the members of parameter `t`
public isolated function toArray(table<MapType> t) returns MapType[] = @java:Method {
    'class: "org.ballerinalang.langlib.table.ToArray",
    name: "toArray"
} external;

# Returns the next available integer key.
#
# This is maximum used key value + 1, or 0 if no key used
# XXX should it be 0, if the maximum used key value is < 0?
# Provides similar functionality to auto-increment
#
# ```ballerina
# table<record {|readonly int id; string name;|}> key(id) users = table [
#     {id: 1, name: "Jo"},
#     {id: 2, name: "Sam"}
# ];
# users.nextKey() ⇒ 3
# ```
#
# + t - the table with a key of type int
# + return - an integer not yet used as a key
public isolated function nextKey(table<MapType> key<int> t) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.table.NextKey",
    name: "nextKey"
} external;
