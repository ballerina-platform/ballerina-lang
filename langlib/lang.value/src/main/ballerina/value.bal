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

# A type parameter that is a subtype of `anydata`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type AnydataType anydata;

# Returns a clone of `v`.
# A clone is a deep copy that does not copy immutable subtrees.
# A clone can therefore safely be used concurrently with the original.
# It corresponds to the Clone(v) operation,
# defined in the Ballerina Language Specification.
#
# + v - source value
# + return - clone of `v`
public isolated function clone(AnydataType v) returns AnydataType = @java:Method {
    'class: "org.ballerinalang.langlib.value.Clone",
    name: "clone"
} external;

# Returns a clone of `v` that is read-only, i.e. immutable.
# It corresponds to the ImmutableClone(v) operation,
# defined in the Ballerina Language Specification.
#
# + v - source value
# + return - immutable clone of `v`
public isolated function cloneReadOnly(AnydataType v) returns AnydataType = @java:Method {
    'class: "org.ballerinalang.langlib.value.CloneReadOnly",
    name: "cloneReadOnly"
} external;

# Constructs a value with a specified type by cloning another value.
# + v - the value to be cloned
# + t - the type for the cloned to be constructed
# + return - a new value that belongs to `t`, or an error if this cannot be done
#
# When `v` is a structural value, the inherent type of the value to be constructed
# comes from `t`. When `t` is a union, it must be possible to determine which
# member of the union to use for the inherent type by following the same rules
# that are used by list constructor expressions and mapping constructor expressions
# with the contextually expected type. If not, then an error is returned.
# The `constructFrom` operation is recursively applied to each member of `v` using
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
#   structural values in the result will be mutable, except for error values
#   (which are always immutable)
# - numeric values can be converted using the NumericConvert abstract operation
# - if a record type descriptor specifies default values, these will be used
#   to supply any missing members
public isolated function cloneWithType(anydata v, typedesc<AnydataType> t) returns AnydataType|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.CloneWithType",
    name: "cloneWithType"
} external;

# Tests whether `v` is read-only, i.e. immutable
# Returns true if read-only, false otherwise.
#
# + v - source value
# + return - true if read-only, false otherwise
public isolated function isReadOnly(anydata v) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.value.IsReadOnly",
    name: "isReadOnly"
} external;

# Performs a direct conversion of a value to a string.
# The conversion is direct in the sense that when applied to a value that is already
# a string it leaves the value unchanged.
#
# + v - the value to be converted to a string
# + return - a string resulting from the conversion
#
# The details of the conversion are specified by the ToString abstract operation
# defined in the Ballerina Language Specification, using the direct style.
public isolated function toString((any|error) v) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.value.ToString",
    name: "toString",
    paramTypes: ["java.lang.Object"]
} external;

# Converts a value to a string that describes the value in Ballerina syntax.
# + v - the value to be converted to a string
# + return - a string resulting from the conversion
#
# If `v` is anydata and does not have cycles, then the result will
# conform to the grammar for a Ballerina expression and when evaluated
# will result in a value that is == to v.
#
# The details of the conversion are specified by the ToString abstract operation
# defined in the Ballerina Language Specification, using the expression style.
public isolated function toBalString(any|error v) returns string = @java:Method {
  'class: "org.ballerinalang.langlib.value.ToBalString",
  name: "toBalString"
} external;

# Parses and evaluates a subset of Ballerina expression syntax.
# + s - the string to be parsed and evaluated
# + return - the result of evaluating the parsed expression, or
# an error if the string cannot be parsed
# The subset of Ballerina expression syntax supported is that produced
# by toBalString when applied to an anydata value.
public isolated function fromBalString(string s) returns anydata|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromBalString",
    name: "fromBalString"
} external;

// JSON conversion

# Converts a value of type `anydata` to `json`.
# This does a deep copy of `v` converting values that do
# not belong to json into values that do.
# A value of type `xml` is converted into a string as if
# by the `toString` function.
# A value of type `table` is converted into a list of
# mappings one for each row.
# The inherent type of arrays in the return value will be
# `json[]` and of mappings will be `map<json>`.
# A new copy is made of all structural values, including
# immutable values.
#
# + v - anydata value
# + return - representation of `v` as value of type json
# This panics if `v` has cycles.
public isolated function toJson(anydata v) returns json = @java:Method {
    'class: "org.ballerinalang.langlib.value.ToJson",
    name: "toJson"
} external;

# Returns the string that represents `v` in JSON format.
# `v` is first converted to `json` as if by the `toJson` function.
#
# + v - anydata value
# + return - string representation of json
public isolated function toJsonString(anydata v) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.value.ToJsonString",
    name: "toJsonString"
} external;

# Parses a string in JSON format and returns the the value that it represents.
# All integer numbers in the JSON will be represented as integer values.
# All decimal numbers except -0.0 in the JSON will be represent as decimal values.
# -0.0 in the JSON will be represent as float value.
# Returns an error if the string cannot be parsed.
#
# + str - string representation of json
# + return - `str` parsed to json or error
public isolated function fromJsonString(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonString",
    name: "fromJsonString"
} external;

# Parses a string in JSON format and returns the value that it represents.
# All numbers in the JSON will be represented as float values.
# Returns an error if the string cannot be parsed.
#
# + str - string representation of json
# + return - `str` parsed to json or error
public isolated function fromJsonFloatString(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonFloatString",
    name: "fromJsonFloatString"
} external;

# Parses a string in JSON format and returns the value that it represents.
# All numbers in the JSON will be represented as decimal values.
# Returns an error if the string cannot be parsed.
#
# + str - string representation of json
# + return - `str` parsed to json or error
public isolated function fromJsonDecimalString(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonDecimalString",
    name: "fromJsonDecimalString"
} external;

# Converts a value of type json to a user-specified type.
# This works the same as `cloneWithType`,
# except that it also does the inverse of the conversions done by `toJson`.
#
# + v - json value
# + t - type to convert to
# + return - value belonging to `t`, or error if this cannot be done
public isolated function fromJsonWithType(json v, typedesc<anydata> t)
    returns t|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonWithType",
    name: "fromJsonWithType"
} external;

# Converts a string in JSON format to a user-specified type.
# This is a combination of `fromJsonString` followed by
# `fromJsonWithType`.
# + str - string in JSON format
# + t - type to convert to
# + return - value belonging to `t`, or error if this cannot be done
public isolated function fromJsonStringWithType(string str, typedesc<anydata> t) returns t|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.FromJsonStringWithType",
    name: "fromJsonStringWithType"
} external;

# Merges two json values.
#
# + j1 - json value
# + j2 - json value
# + return - the merge of `j1` with `j2` or an error if the merge fails
#
# The merge of `j1` with `j2` is defined as follows:
# - if `j1` is `()`, then the result is `j2`
# - if `j2` is `()`, then the result is `j1`
# - if `j1` is a mapping and `j2` is a mapping, then for each entry [k, j] in j2,
#   set `j1[k]` to the merge of `j1[k]` with `j`
#     - if `j1[k]` is undefined, then set `j1[k]` to `j`
#     - if any merge fails, then the merge of `j1` with `j2` fails
#     - otherwise, the result is `j1`.
# - otherwise, the merge fails
# If the merge fails, then `j1` is unchanged.
public isolated function mergeJson(json j1, json j2) returns json|error = @java:Method {
    'class: "org.ballerinalang.langlib.value.MergeJson",
    name: "mergeJson"
} external;

public isolated function ensureType(any|error v, typedesc<any> t) returns any|error =  @java:Method {
    'class: "org.ballerinalang.langlib.value.EnsureType",
    name: "ensureType"
} external;
