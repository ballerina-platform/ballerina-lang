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

# Constructs a value with a specified type by copying another value.
# + t - the type for the copy to be constructed
# + v - the value to be copied
# + return - a new value that belongs to the type of `t`, or an error if this cannot be done
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
# - the graph structure of `v` is not preserved; the result will always be a tree;
#   an error will be returned if `v` has cycles
# - immutable structural values are copied rather being returned as is; all
#   structural values in the result will be mutable, except for error values
#   (which are always immutable)
# - numeric values can be converted using the NumericConvert abstract operation
# - if a record type descriptor specifies default values, these will be used
#   to supply any missing members
public function constructFrom(typedesc<AnydataType> t, anydata v) returns AnydataType|error = external;
