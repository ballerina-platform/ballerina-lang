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

import ballerina/lang.__internal as internal;
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

# A type parameter that is a subtype of `anydata|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type PureType any|error;

# Returns the number of members of an array.
#
# + arr - the array
# + return - number of members in `arr`
public isolated function length((any|error)[] arr) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.array.Length",
    name: "length"
} external;

# Returns an iterator over an array.
#
# + arr - the array
# + return - a new iterator object that will iterate over the members of `arr`.
public isolated function iterator(Type[] arr) returns object {
    public isolated function next() returns record {|
        Type value;
    |}?;
} {
    ArrayIterator arrIterator = new(arr);
    return arrIterator;
}

# Returns a new array consisting of index and member pairs.
#
# + arr - the array
# + return - array of index, member pairs
public isolated function enumerate(Type[] arr) returns [int, Type][] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Enumerate",
    name: "enumerate"
} external;

// Functional iteration

# Applies a function to each member of an array and returns an array of the results.
#
# + arr - the array
# + func - a function to apply to each member
# + return - new array containing result of applying `func` to each member of `arr` in order
public isolated function 'map(Type[] arr, @isolatedParam function(Type val) returns Type1 func) returns Type1[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Map",
    name: "map"
} external;

# Applies a function to each member of an array.
# The parameter `func` is applied to each member of array `arr` in order.
#
# + arr - the array
# + func - a function to apply to each member
public isolated function forEach(Type[] arr, @isolatedParam function(Type val) returns () func) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.ForEach",
    name: "forEach"
} external;

# Selects the members from an array for which a function returns true.
#
# + arr - the array
# + func - a predicate to apply to each member to test whether it should be selected
# + return - new array only containing members of `arr` for which `func` evaluates to true
public isolated function filter(Type[] arr, @isolatedParam function(Type val) returns boolean func) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Filter",
    name: "filter"
} external;

# Combines the members of an array using a combining function.
# The combining function takes the combined value so far and a member of the array,
# and returns a new combined value.
#
# + arr - the array
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of `arr` using `func`
#
# For example
# ```
# reduce([1, 2, 3], function (int total, int n) returns int { return total + n; }, 0)
# ```
# is the same as `sum(1, 2, 3)`.
public isolated function reduce(Type[] arr, @isolatedParam function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 = @java:Method {
    'class: "org.ballerinalang.langlib.array.Reduce",
    name: "reduce"
} external;

# Returns a subarray starting from `startIndex` (inclusive) to `endIndex` (exclusive).
#
# + arr - the array
# + startIndex - index of first member to include in the slice
# + endIndex - index of first member not to include in the slice
# + return - array slice within specified range
public isolated function slice(Type[] arr, int startIndex, int endIndex = arr.length()) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Slice",
    name: "slice"
} external;

# Removes a member of an array.
#
# + arr - the array
# + index - index of member to be removed from `arr`
# + return - the member of `arr` that was at `index`
# This removes the member of `arr` with index `index` and returns it.
# It panics if there is no such member.
public isolated function remove(Type[] arr, int index) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.array.Remove",
    name: "remove"
} external;

# Removes all members of an array.
# + arr - the array
#  Panics if any member cannot be removed.
public isolated function removeAll((any|error)[] arr) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.RemoveAll",
    name: "removeAll"
} external;

# Changes the length of an array.
#
# + arr - the array of which to change the length
# + length - new length
# `setLength(arr, 0)` is equivalent to `removeAll(arr)`.
public isolated function setLength((any|error)[] arr, int length) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.SetLength",
    name: "setLength"
} external;

# Returns the index of first member of `arr` that is equal to `val` if there is one.
# Returns `()` if not found.
# Equality is tested using `==`.
#
# + arr - the array
# + val - member to search for
# + startIndex - index to start the search from
# + return - index of the member if found, else `()`
public isolated function indexOf(PureType[] arr, PureType val, int startIndex = 0) returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.array.IndexOf",
    name: "indexOf"
} external;

# Returns the index of last member of `arr` that is equal to `val` if there is one.
# Returns `()` if not found.
# Equality is tested using `==`.
#
# + arr - the array
# + val - member to search for
# + startIndex - index to start searching backwards from
# + return - index of the member if found, else `()`
public isolated function lastIndexOf(PureType[] arr, PureType val, int startIndex = arr.length() - 1) returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.array.LastIndexOf",
    name: "lastIndexOf"
} external;

# Reverses the order of the members of an array.
#
# + arr - the array to be reversed
# + return - `arr` with its members in reverse order
public isolated function reverse(Type[] arr) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Reverse",
    name: "reverse"
} external;

# Direction for `sort` function.
public enum SortDirection {
   ASCENDING = "ascending",
   DESCENDING = "descending"
}

# A type parameter that is a subtype of `()|boolean|int|float|decimal|string`.
type BasicType ()|boolean|int|float|decimal|string;

# Any ordered type is a subtype of this.
type OrderedType BasicType|BasicType[];

// TO DO: Add this when cyclic type reference in union type definitions is supported
//# Any ordered type is a subtype of this.
//public type OrderedType ()|boolean|int|float|decimal|string|OrderedType[];

# Sorts an array.
# If the member type of the array is not sorted, then the `key` function
# must be specified.
# Sorting works the same as with the `sort` clause of query expressions.
#
# + arr - the array to be sorted;
# + direction - direction in which to sort
# + key - function that returns a key to use to sort the members
# + return - a new array consisting of the members of `arr` in sorted order
public isolated function sort(Type[] arr, SortDirection direction = ASCENDING,
        (isolated function(Type val) returns OrderedType)? key = ()) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Sort",
    name: "sort"
} external;

// Stack-like methods (JavaScript, Perl)
// panic on fixed-length array
// compile-time error if known to be fixed-length

# Removes and returns the last member of an array.
# The array must not be empty.
#
# + arr - the array
# + return - removed member
public isolated function pop(Type[] arr) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.array.Pop",
    name: "pop"
} external;

# Adds values to the end of an array.
#
# + arr - the array
# + vals - values to add to the end of the array
public isolated function push(Type[] arr, Type... vals) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.Push",
    name: "push"
} external;

// Queue-like methods (JavaScript, Perl, shell)
// panic on fixed-length array
// compile-time error if known to be fixed-length

# Removes and returns first member of an array.
# The array must not be empty.
#
# + arr - the array
# + return - the value that was the first member of the array
public isolated function shift(Type[] arr) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.array.Shift",
    name: "shift"
} external;

# Adds values to the start of an array.
# The values newly added to the array will be in the same order
# as they are in `vals`.
#
# + arr - the array
# + vals - values to add to the start of the array
public isolated function unshift(Type[] arr, Type... vals) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.Unshift",
    name: "unshift"
} external;

// Conversion

# Returns the string that is the Base64 representation of an array of bytes.
# The representation is the same as used by a Ballerina Base64Literal.
# The result will contain only characters  `A..Z`, `a..z`, `0..9`, `+`, `/` and `=`.
# There will be no whitespace in the returned string.
#
# + arr - the array
# + return - Base64 string representation
public isolated function toBase64(byte[] arr) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.array.ToBase64",
    name: "toBase64"
} external;

# Returns the byte array that a string represents in Base64.
# `str` must consist of the characters `A..Z`, `a..z`, `0..9`, `+`, `/`, `=`
# and whitespace as allowed by a Ballerina Base64Literal.
#
# + str - Base64 string representation
# + return - the byte array or error
public isolated function fromBase64(string str) returns byte[]|error = @java:Method {
    'class: "org.ballerinalang.langlib.array.FromBase64",
    name: "fromBase64"
} external;

# Returns the string that is the Base16 representation of an array of bytes.
# The representation is the same as used by a Ballerina Base16Literal.
# The result will contain only characters  `0..9`, `a..f`.
# There will be no whitespace in the returned string.
#
# + arr - the array
# + return - Base16 string representation
public isolated function toBase16(byte[] arr) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.array.ToBase16",
    name: "toBase16"
} external;

# Returns the byte array that a string represents in Base16.
# `str` must consist of the characters `0..9`, `A..F`, `a..f`
# and whitespace as allowed by a Ballerina Base16Literal.
#
# + str - Base16 string representation
# + return - the byte array or error
public isolated function fromBase16(string str) returns byte[]|error = @java:Method {
    'class: "org.ballerinalang.langlib.array.FromBase16",
    name: "fromBase16"
} external;

# Returns a stream from the given array.
#
# + arr - The array from which the stream is created
# + return - The stream representation of the array `arr`
public function toStream(Type[] arr) returns stream<Type> {
    return internal:construct(internal:getElementType(typeof arr), iterator(arr));
}
