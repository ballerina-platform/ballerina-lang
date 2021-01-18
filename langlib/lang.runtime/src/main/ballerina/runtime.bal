// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.'array as lang_array;
import ballerina/lang.'value as lang_value;

# A listener that is dynamically registered with a module.
public type DynamicListener object {
    public function 'start() returns error?;
    public function gracefulStop() returns error?;
    public function immediateStop() returns error?;
};

# Represents a data holder of the current call stack element.
#
# + callableName - Callable name
# + moduleName - Module name
# + fileName - File name
# + lineNumber - Line number
type CallStackElement record {|
    string callableName;
    string moduleName;
    string fileName;
    int lineNumber;
|};

# Register a listener object with a module.
# + listener - the listener object to be registered. The listener becomes a module listener of the module from which
#       this function is called.
public function registerListener(DynamicListener 'listener) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Registry"
} external;

# Deregister a listener from a module.
# + listener - the listener object to be unregistered. The `listener` ceases to be a module listener of the module from
# which this function is called.
public function deregisterListener(DynamicListener 'listener) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Registry"
} external;

# Halts the current strand for a predefined amount of time.
#
# + seconds - An amount of time to sleep in seconds
public isolated function sleep(decimal seconds) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Sleep"
} external;

# Returns a stack trace for the current call stack.
#
# + return - An array representing the current call stack
public isolated function getStackTrace() returns StackFrame[] {
    StackFrame[] stackFrame = [];
    int i = 0;
    CallStackElement[] callStackElements = externGetStackTrace();
    lang_array:forEach(callStackElements, function (CallStackElement callStackElement) {
                                stackFrame[i] = new java:StackFrameImpl(callStackElement.callableName,
                                callStackElement.moduleName, callStackElement.fileName, callStackElement.lineNumber);
                                i += 1;
                            });
    return stackFrame;
}

# Represents a stack frame.
// todo use readonly qualifier once #27501 fixed
# public type StackFrame readonly & object {
public type StackFrame object {

   # Returns a string representing the StackFrame.
   #
   # + return - A StackFrame as a `string`
   public function toString() returns string;
};

isolated function externGetStackTrace() returns CallStackElement[] = @java:Method {
    name: "getStackTrace",
    'class: "org.ballerinalang.langlib.runtime.GetStackTrace"
} external;
