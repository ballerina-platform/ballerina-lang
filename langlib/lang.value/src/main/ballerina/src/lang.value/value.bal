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

# A type parameter that is a subtype of `anydata`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type AnydataType anydata;

# Returns a clone of `v`.
# A clone is a deep copy that does not copy immutable subtrees.
# A clone can therefore safely be used concurrently with the original.
# It corresponds to the Clone(v) abstract operation,
# defined in the Ballerina Language Specification.
#
# + v - source value
# + return - clone of `v`
public function clone(AnydataType v) returns AnydataType = external;

# Returns a clone of `v` that is read-only, i.e. immutable.
# It corresponds to the ImmutableClone(v) abstract operation,
# defined in the Ballerina Language Specification.
#
# + v - source value
# + return - immutable clone of `v`
public function cloneReadOnly(AnydataType v) returns AnydataType = external;

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
public function cloneWithType(anydata v, typedesc<AnydataType> t) returns AnydataType|error = external;

# Tests whether `v` is read-only, i.e. immutable
# Returns true if read-only, false otherwise.
#
# + v - source value
# + return - true if read-only, false otherwise
public function isReadOnly(anydata v) returns boolean = external;

# Performs a minimal conversion of a value to a string.
# The conversion is minimal in particular in the sense
# that the conversion applied to a value that is already
# a string does nothing.
# + v - the value to be converted to a string
# + return - a string resulting from the conversion
#
# The result of `toString(v)` is as follows:
#
# - if `v` is a string, then returns `v`
# - if `v` is `()`, then returns an empty string
# - if `v` is boolean, then the string `true` or `false`
# - if `v` is an int, then return `v` represented as a decimal string
# - if `v` is a float or decimal, then return `v` represented as a decimal string,
#   with a decimal point only if necessary, but without any suffix indicating the type of `v`;
#   return `NaN`, `Infinity` for positive infinity, and `-Infinity` for negative infinity
# - if `v` is a list, then returns the results toString on each member of the list
#   separated by a space character
# - if `v` is a map, then returns key=value for each member separated by a space character
# - if `v` is xml, then returns `v` in XML format (as if it occurred within an XML element)
# - if `v` is table, TBD
# - if `v` is an error, then a string consisting of the following in order
#     1. the string `error`
#     2. a space character
#     3. the reason string
#     4. if the detail record is non-empty
#         1. a space character
#         2. the result of calling toString on the detail record
# - if `v` is an object, then
#     - if `v` provides a `toString` method with a string return type and no required methods,
#       then the result of calling that method on `v`
#     - otherwise, `object` followed by some implementation-dependent string
# - if `v` is any other behavioral type, then the identifier for the behavioral type
#   (`function`, `future`, `service`, `typedesc` or `handle`)
#   followed by some implementation-dependent string
#
# Note that `toString` may produce the same string for two Ballerina values
# that are not equal (in the sense of the `==` operator).
public function toString((any|error) v) returns string = external;

// JSON conversion

# Returns the string that represents `v` in JSON format.
#
# + v - json value
# + return - string representation of json
public function toJsonString(json v) returns string = external;

# Parses a string in JSON format and returns the the value that it represents.
# All numbers in the JSON will be represented as float values.
# Returns an error if the string cannot be parsed.
#
# + str - string representation of json
# + return - `str` parsed to json or error
public function fromJsonString(string str) returns json|error = external;

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
public function mergeJson(json j1, json j2) returns json|error = external;
