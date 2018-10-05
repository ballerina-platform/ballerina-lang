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

# Representation of `NullReferenceException`
#
# + message - error message
# + cause - optional error cause
public type NullReferenceException record {
    string message;
    error? cause;
    !...
};

# Representation of `IllegalStateException`
#
# + message - error message
# + cause - optional error cause
public type IllegalStateException record {
    string message;
    error? cause;
    !...
};

# Representation of `CallStackElement`
#
# + callableName - Callable name
# + packageName - Package name
# + fileName - File name
# + lineNumber - Line number
public type CallStackElement record {
    string callableName;
    string packageName;
    string fileName;
    int lineNumber;
    !...
};

# Retrieves the Call Stack
#
# + return - Array of `CallStackElement` elements
public extern function getCallStack() returns CallStackElement[];

# Retrieves the Call Stack Frame for a particular error
#
# + e - optional `error` instance
# + return - `CallStackElement` instance
public extern function getErrorCallStackFrame(error? e) returns CallStackElement;

# Representation of `CallFailedException`
#
# + message - Error message
# + cause - optional `error` instance
# + causes - optional array of `error` instances
public type CallFailedException record {
    string message;
    error? cause;
    error[]? causes;
    !...
};
