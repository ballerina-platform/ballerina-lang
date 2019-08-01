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

# A type parameter that is a subtype of `anydata|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type PureType anydata|error;

# Represent the iterator type returned when `iterator` method is invoked.
type ArrayIterator object {

    private Type[] m;

    public function __init(Type[] m) {
        self.m = m;
    }

    # Return next member or nil if end of iteration is reached.
    public function next() returns record {|
        Type value;
    |}? = external;
};

# Returns the number of members contained in `arr`.
#
# + arr - the array
# + return - number of members in the array
public function length((any|error)[] arr) returns int = external;

# Returns an iterator over the members of `arr`
#
# + arr - the array
# + return - iterator object
public function iterator(Type[] arr) returns abstract object {
    public function next() returns record {|
        Type value;
    |}?;
    } {
    ArrayIterator arrIterator = new(arr);
    return arrIterator;
}

# Returns a new array comprising of position and member pairs.
#
# + arr - the array
# + return - array of position, member pair
public function enumerate(Type[] arr) returns [int, Type][] = external;

# Returns a new array applying function `func` to each member of array `arr`.
#
# + arr - the array
# + func - a function to apply to each member
# + return - new array containing result of applying function `func` to each member
public function 'map(Type[] arr, function(Type val) returns Type1 func) returns Type1[] = external;

# Apply function `func` to each member of array `arr`.
#
# + arr - the array
# + func - a function to apply to each member
public function forEach(Type[] arr, function(Type val) returns () func) returns () = external;

# Returns a new array constructed from those elements of 'arr' for which `func` returns true.
#
# + arr - the array
# + func - a predicate to apply to each element to determine if it should be included
# + return - new array only containig members which evaluate function 'func' to true
public function filter(Type[] arr, function(Type val) returns boolean func) returns Type[] = external;

# Reduce operate on each member of `arr` using combining function `func` to produce
# a new value combining all members of `arr`.
#
# + arr - the array
# + func - combining function
# + initial - initial value to first evaluation of combining function `func`
# + return - result of applying combining function to each member of the array
#
# Example: Emulating sum function.
# ```
# var ar = [1, 2, 3];
# var a = ar.reduce(function (int i, int j) returns int { return i + j; }, 0);
# ```
#
# Example: Emulating map behavior.
# ```
# var ar = [1, 2, 3];
# int[] newArr = [];
# int[] a = ar.reduce(function (int[] a, int j) returns int[] { a.push(j*2); return a; }, newArr);
# ```
public function reduce(Type[] arr, function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 = external;

// TODO: Add default arg for `endIndex`
# Returns a sub array starting from `startIndex` (inclusive) to `endIndex` (exclusive).
#
# + arr - the array
# + startIndex - index of first member to include in the slice
# + endIndex - index of first member not to include in the slice
# + return - array slice within specified range
public function slice(Type[] arr, int startIndex, int endIndex) returns Type[] = external;

# Removes the member of `arr` and index `i` and returns it.
# Panics if `i` is out of range.
#
# + arr - the array
# + i - index of member to be removed
# + return - removed member
public function remove(Type[] arr, int i) returns Type = external;

# Removes all members of `arr`.
# Panics if any member cannot be removed.
#
# + arr - the array
public function removeAll((any|error)[] arr) returns () = external;

# Increase or decrease the length.
# `setLength(arr, 0)` is equivalent to `removeAll(arr)`.
#
# + arr - the array
# + i - new length
public function setLength((any|error)[] arr, int i) returns () = external;

# Returns the index of first member of `arr` that is equal to `val` if there is one.
# Returns `()` if not found
# Equality is tested using `==`
#
# + arr - the array
# + val - member to search for
# + startIndex - index to start the search from
# + return - index of the member if found, else `()`
public function indexOf(PureType[] arr, PureType val, int startIndex = 0) returns int? = external;

# Reverse the order of the members of `arr`.
# Returns `arr`.
#
# + arr - the array to be reversed
# + return - reversed `arr`
public function reverse(Type[] arr) returns Type[] = external;

# Sort `arr` using `func` to order members.
# Returns `arr`.
#
# + arr - the array
# + func - comparator function
# + return - sorted `arr`
public function sort(Type[] arr, function(Type val1, Type val2) returns int func) returns Type[] = external;

// Stack-like methods (JavaScript, Perl)
// panic on fixed-length array
// compile-time error if known to be fixed-length

# Remove and return the last member of the `arr`.
#
# + arr - the array
# + return - removed member
public function pop(Type[] arr) returns Type = external;

# Add `vals` to end of the `arr` array.
#
# + arr - the array
# + vals - values to add to the end of the array
public function push(Type[] arr, Type... vals) returns () = external;

// Queue-like methods (JavaScript, Perl, shell)
// panic on fixed-length array
// compile-time error if known to be fixed-length

# Remove and return first element of the array `arr`.
#
# + arr - the array
# + return - removed member
public function shift(Type[] arr) returns Type = external;

# Add `vals` to beginig of the array `arr`.
#
# + arr - the array
# + vals - values to add to the start of the array
public function unshift(Type[] arr, Type... vals) returns () = external;

// Conversion

# Returns the string representing `arr` using Base64 encoding.
# The representation is the same as used by a Ballerina Base64Literal.
# The result will contain only characters  `A..Z`, `a..z`, `0..9`, `+`, `/` and `=`.
# There will be no whitespace in the returned string.
#
# + arr - the array
# + return - Base64 string representation
public function toBase64(byte[] arr) returns string = external;

# Returns the byte array that `str` represents in Base64 encoding.
# `str` must consist of the characters `A..Z`, `a..z`, `0..9`, `+`, `/`, `=`
# and whitespace as allowed by a Ballerina Base64Literal.
#
# + str - Base64 string representation
# + return - the byte array or error
public function fromBase64(string str) returns byte[]|error = external;

# Returns the string representing `arr` using Base16.
# The representation is the same as used by a Ballerina Base16Literal.
# The result will contain only characters  `0..9`, `a..f`.
# There will be no whitespace in the returned string.
#
# + arr - the array
# + return - Base16 string representation
public function toBase16(byte[] arr) returns string = external;

# Returns the byte array that `str` represents in Base16.
# `str` must consist of the characters `0..9`, `A..F`, `a..f`
# and whitespace as allowed by a Ballerina Base16Literal.
#
# + str - Base16 string representation
# + return - the byte array or error
public function fromBase16(string str) returns byte[]|error = external;
