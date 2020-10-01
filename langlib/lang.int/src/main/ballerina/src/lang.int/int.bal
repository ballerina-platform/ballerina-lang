// Copyright (c) 2019, 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Built-in subtype that allows signed integers that can be represented in 32 bits using two's complement.
# This allows an int between -2^31 and 2^31 - 1 inclusive.
# i.e. between -2,147,483,648 and 2,147,483,647 inclusive.
@builtinSubtype
public type Signed32 int;

# Built-in subtype that allows non-negative integers that can be represented in 16 bits using two's complement.
# This allows an int between -2^15 and 2^15 - 1 inclusive.
# i.e. between -32,768 and 32,767 inclusive.
@builtinSubtype
public type Signed16 int;

# Built-in subtype that allows non-negative integers that can be represented in 8 bits using two's complement.
# This allows an int between -2^7 and 2^7 - 1 inclusive.
# i.e. between -128 and 127 inclusive.
@builtinSubtype
public type Signed8 int;

# Built-in subtype that allows non-negative integers that can be represented in 32 bits.
# This allows an int between 0 and 2^32 - 1 inclusive,
# i.e. between 0 and 4,294,967,295 inclusive.
@builtinSubtype
public type Unsigned32 int;

# Built-in subtype that allows non-negative integers that can be represented in 16 bits.
# This allows an int between 0 and 2^16 - 1 inclusive,
# i.e. between 0 and 65,535 inclusive.
@builtinSubtype
public type Unsigned16 int;

# Built-in subtype that allows non-negative integers that can be represented in 8 bits.
# This allows an int between 0 and 2^8 - 1 inclusive,
# i.e. between 0 and 255 inclusive.
# This is the same as `byte`.
@builtinSubtype
public type Unsigned8 int;

# Maximum value of type `int`.
public const int MAX_VALUE = 9223372036854775807;
# Minimum value of type `int`.
public const int MIN_VALUE = -9223372036854775807 - 1; // -9223372036854775808 would overflow
# Maximum value of type `Signed32`.
public const int SIGNED32_MAX_VALUE = 2147483647;
# Minimum value of type `Signed32`.
public const int SIGNED32_MIN_VALUE = -2147483648;
# Maximum value of type `Signed16`.
public const int SIGNED16_MAX_VALUE = 32767;
# Minimum value of type `Signed16`.
public const int SIGNED16_MIN_VALUE = -32768;
# Maximum value of type `Signed8`.
public const int SIGNED8_MAX_VALUE = 127;
# Minimum value of type `Signed8`.
public const int SIGNED8_MIN_VALUE = -128;
# Maximum value of type `Unsigned32`.
public const int UNSIGNED32_MAX_VALUE = 4294967295;
# Maximum value of type `Unsigned16`.
public const int UNSIGNED16_MAX_VALUE = 65535;
# Maximum value of type `Unsigned8`.
public const int UNSIGNED8_MAX_VALUE = 255;

// XXX this will panic for the most negative value (-2^63 is an int but +2^63 isn't)
// consistent with policy on integer overflow

# Returns absolute value of an int.
#
# + n - int value to be operated on
# + return - absolute value of `n`
public isolated function abs(int n) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.integer.Abs",
    name: "abs"
} external;

# Returns sum of zero or more int values.
#
# + ns - int values to sum
# + return - sum of all the `ns`; 0 is `ns` is empty
public isolated function sum(int... ns) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.integer.Sum",
    name: "sum"
} external;

# Maximum of one or more int values.
#
# + n - first int value
# + ns - other int values
# + return - maximum value of value of `x` and all the `xs`
public isolated function max(int n, int... ns) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.integer.Max",
    name: "max"
} external;

# Minimum of one or more int values
#
# + n - first int value
# + ns - other int values
# + return - minimum value of `n` and all the `ns`
public isolated function min(int n, int... ns) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.integer.Min",
    name: "min"
} external;

# Returns the integer that `s` represents in decimal.
# Returns error if `s` is not the decimal representation of an integer.
# The first character may be `+` or `-`.
# This is the inverse of `value:toString` applied to an `int`.
#
# + s - string representation of a integer value
# + return - int representation of the argument or error
public isolated function fromString(string s) returns int|error = @java:Method {
    'class: "org.ballerinalang.langlib.integer.FromString",
    name: "fromString"
} external;

# Returns representation of `n` as hexdecimal string.
# There is no `0x` prefix. Lowercase letters a-f are used.
# Negative numbers will have a `-` prefix. No sign for
# non-negative numbers.
#
# + n - int value
# + return - hexadecimal string representation of int value
public isolated function toHexString(int n) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.integer.ToHexString",
    name: "toHexString"
} external;

# Returns the integer that `s` represents in hexadecimal.
# Both uppercase A-F and lowercase a-f are allowed.
# It may start with an optional `+` or `-` sign.
# No `0x` or `0X` prefix is allowed.
# Returns an error if the `s` is not in an allowed format.
#
# + s - hexadecimal string representation of int value
# + return - int value or error
public isolated function fromHexString(string s) returns int|error = @java:Method {
    'class: "org.ballerinalang.langlib.integer.FromHexString",
    name: "fromHexString"
} external;
