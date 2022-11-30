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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# int length = greetings.length();
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
# int[] evenNumbers = [2, 4, 6, 8];
# object {
#     public isolated function next() returns record {|int value;|}?;
# } iterator = evenNumbers.iterator();
# record {|int value;|}? next = iterator.next();
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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# [int, string][] enumeration = greetings.enumerate();
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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# int[] lengths = greetings.'map((greeting) => greeting.length());
#
# Employee[] employees = [
#     {firstName: "Jo", lastName: "David", id: 2121},
#     {firstName: "Emma", id: 2122},
#     {firstName: "John", lastName: "Doe", id: 2123}
# ];
# string[] fullNames =
#     employees.'map(isolated function (Employee employee) returns string {
#         string? lastName = employee.lastName;
#
#         if lastName is () {
#             return employee.firstName;
#         }
#
#         return string `${employee.firstName} ${lastName}`;
#     });
#
# type Employee record {
#     string firstName;
#     string lastName?;
#     int id;
# };
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
# Employee[] employees = [
#     {name: "Jo", salary: 1200, id: 2121},
#     {name: "Emma", id: 2122, salary: ()},
#     {name: "John", salary: 1500, id: 2123}
# ];
#
# employees.forEach(isolated function (Employee employee) {
#     if employee.salary == () {
#         employee.salary = 1000;
#     }
# });
#
# type Employee record {|
#     string name;
#     int id;
#     decimal? salary;
# |};
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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# string[] filteredByLength = greetings.filter((greeting) => greeting.length() > 4);
#
# Employee[] employees = [
#     {name: "Jo", salary: 1200, id: 2121},
#     {name: "Emma", id: 2122, salary: 900},
#     {name: "John", salary: 1500, id: 2123}
# ];
# Employee[] filteredBySalary = employees.filter(isolated function (Employee employee) returns boolean {
#     decimal salary = employee.salary;
#     return salary > 1000d && salary < 1500d;
# });
#
# type Employee record {|
#     string name;
#     int id;
#     decimal salary;
# |};
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
# int[] integers = [1, 2, 3];
# int sum = integers.reduce(isolated function (int total, int next) returns int => total + next, 0);
#
# Employee[] employees = [
#     {name: "Jo", salary: 1200, department: "IT"},
#     {name: "Emma", department: "finance", salary: 900},
#     {name: "John", salary: 1500, department: "IT"}
# ];
# decimal currentTotalSalary = 10500;
# decimal totalSalaryWithITEmployees = employees.reduce(isolated function (decimal total, Employee employee) returns decimal {
#     if employee.department == "IT" {
#         return total + employee.salary;
#     }
#     return total;
# }, currentTotalSalary);
#
# type Employee record {|
#     string name;
#     string department;
#     decimal salary;
# |};
# ```
#
# + arr - the array
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of parameter `arr` using parameter `func`
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
# boolean hasEvenNumber = numbers.some((number) => number % 2 == 0);
#
# Employee[] employees = [
#     {name: "Jo", salary: 1200, department: "IT"},
#     {name: "Emma", department: "finance", salary: 900},
#     {name: "John", salary: 1500, department: "IT"}
# ];
# boolean hasEmployeeWithSalaryInRange = employees.some(isolated function (Employee employee) returns boolean {
#     decimal salary = employee.salary;
#     return salary > 1200d && salary < 1400d;
# });
#
# type Employee record {|
#     string name;
#     string department;
#     decimal salary;
# |};
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

# Returns a subarray using a start index (inclusive) and an end index (exclusive).
#
# ```ballerina
# int[] evenNumbers = [2, 4, 6, 8, 10, 12];
#
# // Slice containing numbers starting from the fourth member to the end of the list.
# int[] slice = evenNumbers.slice(3);
#
# // Slice containing the first four members in the list.
# int[] sliceWithEndIndex = evenNumbers.slice(0, 4);
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

# Tests whether a function returns true for every member of an array.
#
# The parameter `func` is called for each member of `arr` in order unless and until a call returns false.
# When the array is empty, returns true.
#
# ```ballerina
# int[] numbers = [1, 2, 3, 5];
# boolean allEvenNumbers = numbers.every((number) => number % 2 == 0);
#
# Employee[] employees = [
#     {name: "Jo", salary: 1200, department: "IT"},
#     {name: "Emma", department: "finance", salary: 900},
#     {name: "John", salary: 1500, department: "IT"}
# ];
# boolean allEmployeesWithSalaryInRange = employees.every(isolated function (Employee employee) returns boolean {
#     decimal salary = employee.salary;
#     return salary >= 900d && salary < 2000d;
# });
#
# type Employee record {|
#     string name;
#     string department;
#     decimal salary;
# |};
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

# Removes a member of an array.
#
# This removes the member of parameter `arr` with index parameter `index` and returns it.
# It panics if there is no such member.
#
# ```ballerina
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# string removedGreeting = greetings.remove(1);
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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# greetings.removeAll();
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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# greetings.setLength(2);
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
# string[] greetings = ["Hello", "Hola", "Bonjour", "Hola", "Ciao"];
#
# string newGreeting = "guten tag";
# // First index of "guten tag" if it exists in the list.
# int? indexOfNewGreeting = greetings.indexOf(newGreeting);
#
# // First index of "Hola" if it exists in the list, after the second member of the list.
# int? indexOfHola = greetings.indexOf("Hola", 2);
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
# string[] greetings = ["Hello", "Hola", "Bonjour", "Hola", "Ciao", "Hola"];
#
# string newGreeting = "guten tag";
# // Last index of "guten tag" if it exists in the list.
# int? indexOfNewGreeting = greetings.lastIndexOf(newGreeting);
#
# // Last index of "Hola" if it exists in the list, searching backward starting from the
# // second to last member of the list.
# int? indexOfHola = greetings.lastIndexOf("Hola", greetings.length() - 2);
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
# int[] evenNumbers = [2, 4, 6, 8, 10];
# int[] evenNumbersReversed = evenNumbers.reverse();
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
# string[] greetings = ["Hello", "Bonjour", "", "Hola", "Ciao"];
#
# // Sort the list based on Unicode code point order.
# string[] sorted = greetings.sort();
#
# // Sort the list based on Unicode code point descending order.
# string[] sortedDescending = greetings.sort(array:DESCENDING);
#
# // Sort the list in ascending order based on a specified ordering function: order by length of string.
# string[] sortedUsingProvidedFunction = greetings.sort(key = isolated function (string str) returns int {
#                                                         int length = str.length();
#                                                         if length == 0 {
#                                                             return int:MAX_VALUE;
#                                                         }
#                                                         return length;
#                                                     });
#
# // Sort the list in descending order based on a specified ordering function: order by length of string.
# string[] sortedByDescendingLength = greetings.sort(array:DESCENDING, (str) => str.length());
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
# int[] evenNumbers = [2, 4, 6, 8, 10];
# int removedLastMember = evenNumbers.pop();
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
# int[] evenNumbers = [2];
#
# // Push multiple members to the end of the list.
# evenNumbers.push(4, 6);
#
# int[] moreEvenNumbers = [8, 10, 12, 14];
# // Push multiple members to the end of the list using an existing list in a rest argument.
# evenNumbers.push(...moreEvenNumbers);
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
# int[] evenNumbers = [2, 4, 6, 8, 10];
# int removedFirstMember = evenNumbers.shift();
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
# int[] evenNumbers = [14];
#
# // Add multiple members to the start of the list.
# evenNumbers.unshift(10, 12);
#
# int[] moreEvenNumbers = [2, 4, 6, 8];
# // Add multiple members to the start of the list using an existing list in a rest argument.
# evenNumbers.unshift(...moreEvenNumbers);
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
# byte[] byteArray = [104, 101, 108, 108, 111, 32, 98, 97, 108, 108, 101, 114, 105, 110, 97, 32, 33, 33, 33];
# string base64Rep = byteArray.toBase64();
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
# string base64Rep = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
# byte[] byteArray = check array:fromBase64(base64Rep);
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
# byte[] byteArray = [170, 171, 207, 204, 173, 175, 205, 52, 26, 75, 223, 171, 205, 137, 18, 223];
# string base16Rep = byteArray.toBase16();
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
# string base16Rep = "aaabcfccadafcd341a4bdfabcd8912df";
# byte[] byteArray = check array:fromBase16(base16Rep);
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
# string[] greetings = ["Hello", "Bonjour", "Hola", "Ciao"];
# stream<string> greetingsStream = greetings.toStream();
# ```
#
# + arr - The array from which the stream is created
# + return - The stream representation of the array `arr`
public isolated function toStream(Type[] arr) returns stream<Type> {
     return <stream<Type>>internal:construct(internal:getElementType(typeof arr), typeof (), iterator(arr));
}
