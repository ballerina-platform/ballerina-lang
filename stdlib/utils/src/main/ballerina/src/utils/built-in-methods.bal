// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Clone a given value.
#
# + value - Value to be cloned
# + return - Clone of the given value
function clone(any value) returns anydata|error = external;

# Convert simple value to given type.
#
# + convertType - Type to be converted
# + value - Value to be converted
# + return - Converted value
function simpleValueConvert(typedesc<any|error> convertType, any value) returns anydata|error = external;

# Freeze a given value.
#
# + value - Value to be frozen
# + return - Frozen value
function freeze(any|error value) returns anydata|error = external;

# Check freeze status of given value.
#
# + value - Value to check freeze status
# + return - True for a frozen value
function isFrozen(any|error value) returns boolean = external;

//# Get the reason phrase of an error value.
//#
//# + value - Error value
//# + return - Reason phrase
//function reason(error value) returns string = external;
//
//# Get error details of an error value.
//#
//# + value - Error value
//# + return - Error detail
//function detail(error value) returns anydata = external;

# Get a new Iterator
#
# + data - Data collection
# + return - Iterator for the given data
function iterate(any data) returns any = external;

# Get length of given value.
#
# + value - Value to get the length
# + return - Length of the given value
function length(anydata value) returns int = external;

# Get the next value of an iterator.
#
# + iterator - Iterator
# + return - Next value
function next(any iterator) returns anydata|error = external;
