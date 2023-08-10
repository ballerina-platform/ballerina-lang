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

import ballerina/jballerina.java;

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
    string moduleName?;
    string fileName;
    int lineNumber;
|};

# Registers a listener object with a module.
#
# The listener becomes a module listener of the module from which this
# function is called.
#
# ```ballerina
# runtime:DynamicListener ln = object {
#     public function 'start() returns error? {}
#     public function gracefulStop() returns error? {}
#     public function immediateStop() returns error? {}
# };
# 
# runtime:registerListener(ln);
# ```
# 
# + listener - the listener object to be registered
public isolated function registerListener(DynamicListener 'listener) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Registry"
} external;

# Deregisters a listener from a module.
#
# The `listener` ceases to be a module listener of the module from
# which this function is called.
# 
# ```ballerina
# runtime:DynamicListener ln = object {
#     public function 'start() returns error? {}
#     public function gracefulStop() returns error? {}
#     public function immediateStop() returns error? {}
# };
# 
# runtime:deregisterListener(ln);
# ```
# 
# + listener - the listener object to be unregistered
public isolated function deregisterListener(DynamicListener 'listener) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Registry"
} external;

# Halts the current strand for a predefined amount of time.
#
# ```ballerina
# runtime:sleep(5);
# ```
# 
# + seconds - An amount of time to sleep in seconds
public isolated function sleep(decimal seconds) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Sleep"
} external;

# Type representing a stack frame.
#
# A call stack is represented as an array of stack frames.
# This type is also present in lang.error to avoid a dependency.
public type StackFrame readonly & object {
   # Returns a string representing this StackFrame.
   # This must not contain any newline characters.
   # + return - a string
   public function toString() returns string;
};

# Returns a stack trace for the current call stack.
#
# ```ballerina
# runtime:StackFrame[] stackTrace = runtime:getStackTrace();
# ```
# 
# The first member of the array represents the top of the call stack.
# + return - an array representing the current call stack
public isolated function getStackTrace() returns StackFrame[] {
    StackFrame[] stackFrame = [];
    CallStackElement[] callStackElements = externGetStackTrace();

    foreach CallStackElement callStackElement in callStackElements {
        stackFrame.push(new java:StackFrameImpl(callStackElement.callableName,
                                callStackElement.fileName, callStackElement.lineNumber, callStackElement?.moduleName));
    }

    return stackFrame;
}

isolated function externGetStackTrace() returns CallStackElement[] = @java:Method {
    name: "getStackTrace",
    'class: "org.ballerinalang.langlib.runtime.GetStackTrace"
} external;

# Type of the function passed to `onGracefulStop`.
public type StopHandler function() returns error?;

# Registers a function that will be called during graceful shutdown.
# A call to `onGracefulStop` will result in one call to the handler function
# that was passed as an argument; the handler functions will be called
# after calling `gracefulStop` on all registered listeners,
# in reverse order of the corresponding calls to `onGracefulStop`.
# 
# ```ballerina
# runtime:onGracefulStop(function() returns error? {});
# ```
# 
# + handler - function to be called
public isolated function onGracefulStop(StopHandler 'handler) = @java:Method {
    'class: "org.ballerinalang.langlib.runtime.Registry"
} external;
