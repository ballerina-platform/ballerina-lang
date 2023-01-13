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

import ballerina/jballerina.java;

# The type of value to which `clone` and `cloneReadOnly` can be applied.
public type Cloneable readonly|xml|Cloneable[]|map<Cloneable>|table<map<Cloneable>>;

# A type parameter that is a subtype of `Cloneable`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type CloneableType Cloneable|never;

# A type parameter that is a subtype of `anydata`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type AnydataType anydata;

# Returns a clone of a value.
#
# A clone is a deep copy that does not copy immutable subtrees.
# A clone can therefore safely be used concurrently with the original.
# It corresponds to the Clone(v) abstract operation,
# defined in the Ballerina Language Specification.
#
# ```ballerina
# int[] arr = [1, 2, 3, 4];
# int[] clone = arr.clone();
# clone ⇒ [1,2,3,4]
# arr === clone ⇒ false
# ```
#
# + v - source value
# + return - clone of parameter `v`
public isolated function clone(CloneableType v) returns CloneableType = @java:Method {
    'class: "org.ballerinalang.langlib.value.Clone",
    name: "clone"
} external;

# Returns a clone of a value that is read-only, i.e., immutable.
#
# It corresponds to the ImmutableClone(v) abstract operation,
# defined in the Ballerina Language Specification.
#
# ```ballerina
# int[] arr = [1, 2, 3, 4];
# int[] & readonly immutableClone = arr.cloneReadOnly();
# immutableClone ⇒ [1,2,3,4]
# immutableClone is readonly ⇒ true 
# ```
#
# + v - source value
# + return - immutable clone of parameter `v`
public isolated function cloneReadOnly(CloneableType  v) returns CloneableType & readonly = @java:Method {
    'class: "org.ballerinalang.langlib.value.CloneReadOnly",
    name: "cloneReadOnly"
} external;

# Constructs a value with a specified type by cloning another value.
#
# When parameter `v` is a structural value, the inherent type of the value to be constructed
# comes from parameter `t`. When parameter `t` is a union, it must be possible to determine which
# member of the union to use for the inherent type by following the same rules
# that are used by list constructor expressions and mapping constructor expressions
# with the contextually expected type. If not, then an error is returned.
# The `cloneWithType` operation is recursively applied to each member of parameter `v` using
# the type descriptor that the inherent type requires for that member.
#
# Like the Clone abstract operation, this does a deep copy, but differs in
# the following respects:
# - the inherent type of any structural values constructed comes from the specified
#   type descriptor rather than the value being constructed
# - the read-only bit of values and fields comes from the specified type descriptor
# - the graph structure of `v` is not preserved; the result will always be a tree;
#   an error will be returned if `v` has cycles
# - immutable structural values are copied rather being returned as is; all
#   structural values in the result will be mutable.
# - numeric values can be converted using the NumericConvert abstract operation
# - if a record type descriptor specifies default values, these will be used
#   to supply any missing members
#
# ```ballerina
# anydata[] arr = [1, 2, 3, 4];
# int[] intArray = check arr.cloneWithType();
# intArray ⇒ [1,2,3,4]
# arr === intArray ⇒ false
#
# type Vowels string:Char[];
#
# string[] vowels = ["a", "e", "i", "o", "u"];
# vowels.cloneWithType(Vowels) ⇒ ["a","e","i","o","u"]
#
# vowels.cloneWithType(string) ⇒ error
# ```
#
# + v - the value to be cloned
# + t - the type for the cloned to be constructed
# + return - a new value that belongs to parameter `t`, or an error if this cannot be done
public isolated function cloneWithType(anydata v, typedesc<anydata> t = <>) returns t|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.CloneWithType",
    name: "cloneWithType"
} external;


# Safely casts a value to a type.
#
# This casts a value to a type in the same way as a type cast expression,
# but returns an error if the cast cannot be done, rather than panicking.
#
# ```ballerina
# json student = {name: "Jo", subjects: ["CS1212", "CS2021"]};
# json[] subjects = check student.subjects.ensureType();
# subjects ⇒ ["CS1212","CS2021"]
#
# anydata vowel = "I";
# vowel.ensureType(string:Char) ⇒ I;
#
# vowel.ensureType(int) ⇒ error
# ```
#
# + v - the value to be cast
# + t - a typedesc for the type to which to cast it
# + return - `v` cast to the type described by parameter `t`, or an error, if the cast cannot be done
public isolated function ensureType(any|error v, typedesc<any> t = <>) returns t|error =  @java:Method {
    'class: "org.ballerinalang.langlib.value.EnsureType",
    name: "ensureType"
} external;

# Tests whether a value is read-only, i.e., immutable.
#
# Returns true if read-only, false otherwise.
#
# ```ballerina
# int[] scores = <readonly> [21, 12, 33, 45, 81];
# scores.isReadOnly() ⇒ true
#
# string[] sports = ["cricket", "football", "rugby"];
# sports.isReadOnly() ⇒ false
# ```
#
# + v - source value
# + return - true if read-only, false otherwise
# # Deprecated
# This function will be removed in a future release.
@deprecated
public isolated function isReadOnly(anydata v) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.value.IsReadOnly",
    name: "isReadOnly"
} external;

# Performs a direct conversion of a value to a string.
#
# The conversion is direct in the sense that when applied to a value that is already
# a string it leaves the value unchanged.
#
# The details of the conversion are specified by the ToString abstract operation
# defined in the Ballerina Language Specification, using the direct style.
#
# ```ballerina
# decimal value = 12.12d;
# value.toString() ⇒ 12.12
# 
# anydata[] data = [1, "Sam", 12.3f, 12.12d, {value: 12}];
# data.toString() ⇒ [1,"Sam",12.3,12.12,{"value":12}]
# ```
#
# + v - the value to be converted to a string
# + return - a string resulting from the conversion
public isolated function toString((any) v) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.value.ToString",
    name: "toString",
    paramTypes: ["java.lang.Object"]
} external;

# Converts a value to a string that describes the value in Ballerina syntax.
#
# If parameter `v` is anydata and does not have cycles, then the result will
# conform to the grammar for a Ballerina expression and when evaluated
# will result in a value that is == to parameter `v`.
#
# The details of the conversion are specified by the ToString abstract operation
# defined in the Ballerina Language Specification, using the expression style.
#
# ```ballerina
# decimal value = 12.12d;
# value.toBalString() ⇒ 12.12d
# 
# anydata[] data = [1, "Sam", 12.3f, 12.12d, {value: 12}];
# data.toBalString() ⇒ [1,"Sam",12.3,12.12d,{"value":12}]
# ```
#
# + v - the value to be converted to a string
# + return - a string resulting from the conversion
public isolated function toBalString(any v) returns string = @java:Method {
  'class: "org.ballerinalang.langlib.value.ToBalString",
  name: "toBalString"
} external;

# Parses and evaluates a subset of Ballerina expression syntax.
#
# The subset of Ballerina expression syntax supported is that produced
# by toBalString when applied to an anydata value.
#
# ```ballerina
# string a = "12.12d";
# a.fromBalString() ⇒ 12.12
#
# string b = "[1, 2, !]";
# b.fromBalString() ⇒ error
# ```
#
# + s - the string to be parsed and evaluated
# + return - the result of evaluating the parsed expression, or
# an error if the string cannot be parsed
public isolated function fromBalString(string s) returns anydata|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromBalString",
    name: "fromBalString"
} external;

// JSON conversion

# Converts a value of type `anydata` to `json`.
#
# This does a deep copy of parameter `v` converting values that do
# not belong to json into values that do.
# A value of type `xml` is converted into a string as if
# by the `toString` function.
# A value of type `table` is converted into a list of
# mappings one for each row.
# The inherent type of arrays in the return value will be
# `json[]` and of mappings will be `map<json>`.
# A new copy is made of all structural values, including
# immutable values.
# This panics if parameter `v` has cycles.
#
# ```ballerina
# anydata student = {name: "Jo", age: 11};
# student.toJson() ⇒ {"name":"Jo","age":11}
#
# anydata[] array = [];
# array.push(array);
# array.toJson() ⇒ panic
# ```
#
# + v - anydata value
# + return - representation of `v` as value of type json
public isolated function toJson(anydata v) returns json = @java:Method {
    'class: "org.ballerinalang.langlib.value.ToJson",
    name: "toJson"
} external;

# Returns the string that represents a anydata value in JSON format.
#
# parameter `v` is first converted to `json` as if by the function `toJson`.
#
# ```ballerina
# anydata marks = {"Alice": 90, "Bob": 85, "Jo": 91};
# marks.toJsonString() ⇒ {"Alice":90, "Bob":85, "Jo":91}
# ```
#
# + v - anydata value
# + return - string representation of parameter `v` converted to `json`
public isolated function toJsonString(anydata v) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.value.ToJsonString",
    name: "toJsonString"
} external;

# Parses a string in JSON format and returns the value that it represents.
#
# Numbers in the JSON string are converted into Ballerina values of type
# decimal except in the following two cases:
# if the JSON number starts with `-` and is numerically equal to zero, then it is
# converted into float value of `-0.0`;
# otherwise, if the JSON number is syntactically an integer and is in the range representable
# by a Ballerina int, then it is converted into a Ballerina int.
# A JSON number is considered syntactically an integer if it contains neither
# a decimal point nor an exponent.
#
# Returns an error if the string cannot be parsed.
#
# ```ballerina
# "{\"id\": 12, \"name\": \"John\"}".fromJsonString() ⇒ {"id":12,"name":"John"}
#
# "{12: 12}".fromJsonString() ⇒ error
# ```
#
# + str - string in JSON format
# + return - `str` parsed to json or error
public isolated function fromJsonString(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonString",
    name: "fromJsonString"
} external;

# Subtype of `json` that allows only float numbers.
public type JsonFloat ()|boolean|string|float|JsonFloat[]|map<JsonFloat>;

# Parses a string in JSON format, using float to represent numbers.
#
# Returns an error if the string cannot be parsed.
#
# ```ballerina
# "[12, true, 123.4, \"hello\"]".fromJsonFloatString() ⇒ [12.0,true,123.4,"hello"]
#
# "[12, true, 12.5, !]".fromJsonFloatString() ⇒ error
# ```
#
# + str - string in JSON format
# + return - parameter `str` parsed to JsonFloat or error
public isolated function fromJsonFloatString(string str) returns JsonFloat|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonFloatString",
    name: "fromJsonFloatString"
} external;

# Subtype of `json` that allows only decimal numbers.
public type JsonDecimal ()|boolean|string|decimal|JsonDecimal[]|map<JsonDecimal>;

# Parses a string in JSON format, using decimal to represent numbers.
#
# Returns an error if the string cannot be parsed.
#
# ```ballerina
# "[12, true, 123.4, \"hello\"]".fromJsonDecimalString() ⇒ [12.0,true,123.4,"hello"]
#
# "[12, true, 12.5, !]".fromJsonDecimalString() ⇒ error
# ```
#
# + str - string in JSON format
# + return - parameter `str` parsed to JsonDecimal or error
public isolated function fromJsonDecimalString(string str) returns JsonDecimal|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonDecimalString",
    name: "fromJsonDecimalString"
} external;

# Converts a value of type json to a user-specified type.
#
# This works the same as function `cloneWithType`,
# except that it also does the inverse of the conversions done by `toJson`.
#
# ```ballerina
# json arr = [1, 2, 3, 4];
# int[] intArray = check arr.fromJsonWithType();
# intArray ⇒ [1,2,3,4]
#
# type Vowels string:Char[];
#
# json vowels = ["a", "e", "i", "o", "u"];
# vowels.fromJsonWithType(Vowels) ⇒ ["a","e","i","o","u"]
#
# vowels.fromJsonWithType(string) ⇒ error
# ```
#
# + v - json value
# + t - type to convert to
# + return - value belonging to type parameter `t` or error if this cannot be done
public isolated function fromJsonWithType(json v, typedesc<anydata> t = <>)
    returns t|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonWithType",
    name: "fromJsonWithType"
} external;

# Converts a string in JSON format to a user-specified type.
#
# This is a combination of function `fromJsonString` followed by function `fromJsonWithType`.
#
# ```ballerina
# int[] intArray = check "[1, 2, 3, 4]".fromJsonStringWithType(); 
# intArray ⇒ [1,2,3,4]
#
# "2022".fromJsonStringWithType(int) ⇒ 2022
# "2022".fromJsonStringWithType(boolean) ⇒ error
# ```
#
# + str - string in JSON format
# + t - type to convert to
# + return - value belonging to type parameter `t` or error if this cannot be done
public isolated function fromJsonStringWithType(string str, typedesc<anydata> t = <>) returns t|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonStringWithType",
    name: "fromJsonStringWithType"
} external;

# Merges two `json` values.
#
# The merge of parameter `j1` with parameter `j2` is defined as follows:
# - if parameter `j1` is `()`, then the result is parameter `j2`
# - if parameter `j2` is `()`, then the result is parameter `j1`
# - if parameter `j1` is a mapping and parameter `j2` is a mapping, then for each entry [k, j] in parameter `j2`, set `j1[k]` to the merge of `j1[k]` with `j`
#     - if `j1[k]` is undefined, then set `j1[k]` to `j`
#     - if any merge fails, then the merge of parameter `j1` with parameter `j2` fails
#     - otherwise, the result is parameter `j1`.
# - otherwise, the merge fails
# If the merge fails, then parameter `j1` is unchanged.
#
# ```ballerina
# json student = {name: "John", age: 23};
# json location = {city: "Colombo", country: "Sri Lanka"};
# student.mergeJson(location) ⇒ {"name":"John","age":23,"city":"Colombo","country":"Sri Lanka"}
#
# value:mergeJson(student, location) ⇒ {"name":"John","age":23,"city":"Colombo","country":"Sri Lanka"}
#
# json city = "Colombo";
# student.mergeJson(city) ⇒ error
# ```
#
# + j1 - json value
# + j2 - json value
# + return - the merge of parameter `j1` with parameter `j2` or an error if the merge fails
public isolated function mergeJson(json j1, json j2) returns json|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.MergeJson",
    name: "mergeJson"
} external;
