// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

# Representation of `CallStackElement`.
#
# + callableName - Callable name
# + moduleName - Module name
# + fileName - File name
# + lineNumber - Line number
type CallStackElement record {|
    string callableName;
    string moduleName?;
    string fileName;
    int lineNumber;
|};

# Represents an error call stack.
#
# + callStack - call stack
class CallStack {
    public CallStackElement[] callStack = [];
}

isolated function externGetStackTrace(error e) returns CallStack = @java:Method {
    name: "stackTrace",
    'class: "org.ballerinalang.langlib.error.StackTrace"
} external;
