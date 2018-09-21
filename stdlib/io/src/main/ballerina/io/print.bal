// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Prints 'any' value to the STDOUT.
# + a - The value to be printed.
public extern function print(any... a);

# Prints an any value to the STDOUT in a new line.
# + a - The value to be printed.
public extern function println(any... a);

# Returns a formatted string using the specified format string and arguments. Following format specifiers are allowed.
#    b              boolean
#    B              boolean (ALL_CAPS)
#    d              int
#    f              float
#    x              hex
#    X              HEX (ALL_CAPS)
#    s              string (This specifier is applicable for any of the supported types in Ballerina.
#                   These values will be converted to their string representation.)
#
# + format - A format string
# + args   - Arguments referenced by the format specifiers in the format string.
# + return - Formatted string
public extern function sprintf(string format, any... args) returns (string);
