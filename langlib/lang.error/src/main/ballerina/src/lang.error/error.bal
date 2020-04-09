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

# The type to which error detail records must belong.
#
# + message - the error message
# + cause - the error cause
public type Detail record {|
    string message?;
    error cause?;
    (anydata|error)...;
|};

# A type parameter that is a subtype of error `Detail` record type.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
#
# + message - the error message
# + cause - the error cause
@typeParam
type DetailType record {|
    string message?;
    error cause?;
    (anydata|error)...;
|};

# A type parameter that is a subtype of `string` type.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type StringType string;

# Returns the error's reason string.
#
# + e - the error value
# + return - error reason
public function reason(error<StringType> e) returns StringType = external;

# Returns the error's detail record.
# The returned value will be immutable.
# + e - the error value
# + return - error detail value
public function detail(error<string,DetailType> e) returns DetailType = external;

# Returns an object representing the stack trace of the error.
#
# + e - the error value
# + return - a new object representing the stack trace of the error value
public function stackTrace(error e) returns CallStack = external;

# Representation of `CallStackElement`
#
# + callableName - Callable name
# + moduleName - Module name
# + fileName - File name
# + lineNumber - Line number
public type CallStackElement record {|
    string callableName;
    string moduleName;
    string fileName;
    int lineNumber;
|};

# Represent error call stack.
#
# + callStack - call stack
public type CallStack object {
    public CallStackElement[] callStack = [];
};
