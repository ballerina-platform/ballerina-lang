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
import ballerina/jballerina.java;

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
type AnydataType anydata;

# Returns the number of members of an array.
#
# ```ballerina
# ["a", "b", "c", "d"].length() ⇒ 4
# ```
#
# + arr - the array
# + return - number of members in parameter `arr`
public isolated function length((any|error)[] arr) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.array.Length",
    name: "length"
} external;

# Returns an iterator over an array.
#
# ```ballerina
# object {
#     public isolated function next() returns record {|int value;|}?;
# } iterator = [2, 4, 6, 8].iterator();
# iterator.next() ⇒ {"value":2}
# ```
#
# + arr - the array
# + return - a new iterator object that will iterate over the members of parameter `arr`
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
# ```ballerina
# [1, 2, 3, 4].enumerate() ⇒ [[0,1],[1,2],[2,3],[3,4]]
# ```
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
# ```ballerina
# int[] numbers = [0, 1, 2];
# numbers.map(n => n * 2) ⇒ [0,2,4]
# ```
#
# + arr - the array
# + func - a function to apply to each member
# + return - new array containing result of applying parameter `func` to each member of parameter `arr` in order
public isolated function 'map(Type[] arr, @isolatedParam function(Type val) returns Type1 func) returns Type1[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Map",
    name: "map"
} external;

# Applies a function to each member of an array.
#
# The parameter `func` is applied to each member of parameter `arr` in order.
#
# ```ballerina
# int total = 0;
# [1, 2, 3].forEach(function (int i) {
#     total += i;
# });
# total ⇒ 6
# ```
#
# + arr - the array
# + func - a function to apply to each member
public isolated function forEach(Type[] arr, @isolatedParam function(Type val) returns () func) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.ForEach",
    name: "forEach"
} external;

# Selects the members from an array for which a function returns true.
#
# ```ballerina
# int[] numbers = [12, 43, 60, 75, 10];
# numbers.filter(n => n > 50) ⇒ [60,75]
# ```
#
# + arr - the array
# + func - a predicate to apply to each member to test whether it should be selected
# + return - new array only containing members of parameter `arr` for which parameter `func` evaluates to true
public isolated function filter(Type[] arr, @isolatedParam function(Type val) returns boolean func) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Filter",
    name: "filter"
} external;

# Combines the members of an array using a combining function.
#
# The combining function takes the combined value so far and a member of the array,
# and returns a new combined value.
#
# ```ballerina
# [1, 2, 3].reduce(isolated function (int total, int next) returns int => total + next, 0) ⇒ 6
# ```
#
# + arr - the array
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of parameter `arr` using parameter `func`
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

# Tests whether a function returns true for some member of an array.
#
# The parameter `func` is called for each member of parameter `arr` in order unless and until a call returns true.
# When the array is empty, returns false.
#
# ```ballerina
# int[] numbers = [1, 2, 3, 5];
# numbers.some(n => n % 2 == 0) ⇒ true
# ```
#
# + arr - the array
# + func - function to apply to each member
# + return - true if applying parameter `func` returns true for some member of `arr`; otherwise, false
public isolated function some(Type[] arr, @isolatedParam function(Type val) returns boolean func) returns boolean {
    foreach var item in arr {
        if func(item) {
            return true;
        }
    }
    return false;
}

# Tests whether a function returns true for every member of an array.
#
# The parameter `func` is called for each member of `arr` in order unless and until a call returns false.
# When the array is empty, returns true.
#
# ```ballerina
# int[] numbers = [1, 2, 3, 5];
# numbers.every(n => n % 2 == 0) ⇒ false
# ```
#
# + arr - the array
# + func - function to apply to each member
# + return - true if applying parameter func returns true for every member of `arr`; otherwise, false
public isolated function every(Type[] arr, @isolatedParam function(Type val) returns boolean func) returns boolean {
    foreach var item in arr {
        if !func(item) {
            return false;
        }
    }
    return true;
}

# Returns a subarray using a start index (inclusive) and an end index (exclusive).
#
# ```ballerina
# int[] numbers = [2, 4, 6, 8, 10, 12];
#
# numbers.slice(3) ⇒ [8,10,12]
#
# numbers.slice(0, 4) ⇒ [2,4,6,8]
#
# numbers.slice(0, 10) ⇒ panic
# ```
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
# This removes the member of parameter `arr` with index parameter `index` and returns it.
# It panics if there is no such member.
#
# ```ballerina
# int[] numbers = [2, 4, 6, 8];
#
# numbers.remove(1) ⇒ 4
#
# numbers.remove(7) ⇒ panic
# ```
#
# + arr - the array
# + index - index of member to be removed from parameter `arr`
# + return - the member of parameter `arr` that was at parameter `index`
public isolated function remove(Type[] arr, int index) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.array.Remove",
    name: "remove"
} external;

# Removes all members of an array.
#
# Panics if any member cannot be removed.
#
# ```ballerina
# int[] numbers = [2, 4, 6, 8];
# numbers.removeAll();
# numbers ⇒ []
#
# int[2] values = [1, 2];
# values.removeAll() ⇒ panic
# ```
#
# + arr - the array
public isolated function removeAll((any|error)[] arr) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.RemoveAll",
    name: "removeAll"
} external;

# Changes the length of an array.
#
# `setLength(arr, 0)` is equivalent to `removeAll(arr)`.
#
# ```ballerina
# int[] numbers = [2, 4, 6, 8];
#
# numbers.setLength(2);
# numbers ⇒ [2,4]
#
# numbers.setLength(0);
# numbers ⇒ []
# ```
#
# + arr - the array of which to change the length
# + length - new length
public isolated function setLength((any|error)[] arr, int length) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.SetLength",
    name: "setLength"
} external;

# Returns the index of first member of an array that is equal to a given value if there is one.
# Returns `()` if not found.
# Equality is tested using `==`.
#
# ```ballerina
# string[] strings = ["a", "b", "d", "b", "d"];
#
# strings.indexOf("e") is () ⇒ true
#
# strings.indexOf("b") ⇒ 1
#
# strings.indexOf("b", 2) ⇒ 3
# ```
#
# + arr - the array
# + val - member to search for
# + startIndex - index to start the search from
# + return - index of the member if found, else `()`
public isolated function indexOf(AnydataType[] arr, AnydataType val, int startIndex = 0) returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.array.IndexOf",
    name: "indexOf"
} external;

# Returns the index of last member of an array that is equal to a given value if there is one.
# Returns `()` if not found.
# Equality is tested using `==`.
#
# ```ballerina
# string[] strings = ["a", "b", "d", "b", "d", "b"];
#
# strings.lastIndexOf("e") is () ⇒ true
#
# strings.lastIndexOf("b") ⇒ 5
#
# strings.lastIndexOf("b", strings.length() - 2) ⇒ 3
# ```
#
# + arr - the array
# + val - member to search for
# + startIndex - index to start searching backwards from
# + return - index of the member if found, else `()`
public isolated function lastIndexOf(AnydataType[] arr, AnydataType val, int startIndex = arr.length() - 1) returns int? = @java:Method {
    'class: "org.ballerinalang.langlib.array.LastIndexOf",
    name: "lastIndexOf"
} external;

# Reverses the order of the members of an array.
#
# ```ballerina
# [2, 4, 12, 8, 10].reverse() ⇒ [10,8,12,4,2]
# ```
#
# + arr - the array to be reversed
# + return - parameter `arr` with its members in reverse order
public isolated function reverse(Type[] arr) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Reverse",
    name: "reverse"
} external;

# Direction for `sort` function.
public enum SortDirection {
   ASCENDING = "ascending",
   DESCENDING = "descending"
}

# Any ordered type is a subtype of this.
public type OrderedType ()|boolean|int|float|decimal|string|OrderedType[];

# Sorts an array.
#
# If the member type of the array is not ordered, then the parameter `key` function
# must be specified.
# Sorting works the same as with the parameter `sort` clause of query expressions.
#
# ```ballerina
# string[] strings = ["c", "a", "B"];
#
# strings.sort() ⇒ ["B","a","c"]
#
# strings.sort("descending") ⇒ ["c","a","B"]
#
# strings.sort(key = string:toLowerAscii) ⇒ ["a","B","c"]
#
# strings.sort("descending", string:toLowerAscii) ⇒ ["c","B","a"]
# ```
#
# + arr - the array to be sorted;
# + direction - direction in which to sort
# + key - function that returns a key to use to sort the members
# + return - a new array consisting of the members of parameter `arr` in sorted order
public isolated function sort(Type[] arr, SortDirection direction = ASCENDING,
        (isolated function(Type val) returns OrderedType)? key = ()) returns Type[] = @java:Method {
    'class: "org.ballerinalang.langlib.array.Sort",
    name: "sort"
} external;

// Stack-like methods (JavaScript, Perl)
// panic on fixed-length array
// compile-time error if known to be fixed-length

# Removes and returns the last member of an array.
#
# The array must not be empty.
#
# ```ballerina
# int[] numbers = [2, 4, 6, 8, 10];
# numbers.pop() ⇒ 10
#
# int[] values = [];
# values.pop() ⇒ panic
# ```
#
# + arr - the array
# + return - removed member
public isolated function pop(Type[] arr) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.array.Pop",
    name: "pop"
} external;

# Adds values to the end of an array.
#
# ```ballerina
# int[] numbers = [2];
#
# numbers.push(4, 6);
# numbers ⇒ [2,4,6]
#
# int[] moreNumbers = [8, 10, 12, 14];
# numbers.push(...moreNumbers);
# numbers ⇒ [2,4,6,8,10,12,14]
# ```
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
#
# The array must not be empty.
#
# ```ballerina
# int[] numbers = [2, 4, 6, 8, 10];
# numbers.shift() ⇒ 2
#
# int[] values = [];
# values.shift() ⇒ panic
# ```
#
# + arr - the array
# + return - the value that was the first member of the array
public isolated function shift(Type[] arr) returns Type = @java:Method {
    'class: "org.ballerinalang.langlib.array.Shift",
    name: "shift"
} external;

# Adds values to the start of an array.
#
# The values newly added to the array will be in the same order
# as they are in parameter `vals`.
#
# ```ballerina
# int[] numbers = [14];
#
# numbers.unshift(10, 12);
# numbers ⇒ [10,12,14]
#
# int[] moreNumbers = [2, 4, 6, 8];
# numbers.unshift(...moreNumbers);
# numbers ⇒ [2,4,6,8,10,12,14]
# ```
# 
# + arr - the array
# + vals - values to add to the start of the array
public isolated function unshift(Type[] arr, Type... vals) returns () = @java:Method {
    'class: "org.ballerinalang.langlib.array.Unshift",
    name: "unshift"
} external;

// Conversion

# Returns the string that is the Base64 representation of an array of bytes.
#
# The representation is the same as used by a Ballerina Base64Literal.
# The result will contain only characters  `A..Z`, `a..z`, `0..9`, `+`, `/` and `=`.
# There will be no whitespace in the returned string.
#
# ```ballerina
# byte[] byteArray = [104, 101, 108, 108, 111, 32, 98, 97];
# byteArray.toBase64() ⇒ aGVsbG8gYmE=
# ```
#
# + arr - the array
# + return - Base64 string representation
public isolated function toBase64(byte[] arr) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.array.ToBase64",
    name: "toBase64"
} external;

# Returns the byte array that a string represents in Base64.
#
# parameter `str` must consist of the characters `A..Z`, `a..z`, `0..9`, `+`, `/`, `=`
# and whitespace as allowed by a Ballerina Base64Literal.
#
# ```ballerina
# array:fromBase64("aGVsbG8gYmE=") ⇒ [104,101,108,108,111,32,98,97]
#
# array:fromBase64("aGVsbG8gYmE--") ⇒ error
# ```
#
# + str - Base64 string representation
# + return - the byte array or error
public isolated function fromBase64(string str) returns byte[]|error = @java:Method {
    'class: "org.ballerinalang.langlib.array.FromBase64",
    name: "fromBase64"
} external;

# Returns the string that is the Base16 representation of an array of bytes.
#
# The representation is the same as used by a Ballerina Base16Literal.
# The result will contain only characters  `0..9`, `a..f`.
# There will be no whitespace in the returned string.
#
# ```ballerina
# byte[] byteArray = [170, 171, 207, 204, 173, 175, 205];
# byteArray.toBase16() ⇒ aaabcfccadafcd
# ```
#
# + arr - the array
# + return - Base16 string representation
public isolated function toBase16(byte[] arr) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.array.ToBase16",
    name: "toBase16"
} external;

# Returns the byte array that a string represents in Base16.
#
# `str` must consist of the characters `0..9`, `A..F`, `a..f`
# and whitespace as allowed by a Ballerina Base16Literal.
#
# ```ballerina
# array:fromBase16("aaabcfccadafcd") ⇒ [170,171,207,204,173,175,205]
#
# array:fromBase16("aaabcfccadafcd==") ⇒ error
# ```
#
# + str - Base16 string representation
# + return - the byte array or error
public isolated function fromBase16(string str) returns byte[]|error = @java:Method {
    'class: "org.ballerinalang.langlib.array.FromBase16",
    name: "fromBase16"
} external;

# Returns a stream from the given array.
#
# ```ballerina
# stream<string> strm = ["a", "b", "c", "d"].toStream();
# strm.next() ⇒ {"value":"a"}
# ```
#
# + arr - The array from which the stream is created
# + return - The stream representation of the array `arr`
# The returned stream will use an iterator over `arr` and
# will therefore handle mutation of `arr` in the same way
# as an iterator does.
public isolated function toStream(Type[] arr) returns stream<Type> {
     return <stream<Type>>internal:construct(internal:getElementType(typeof arr), typeof (), iterator(arr));
}
