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

import ballerina/lang.runtime as runtime;
import ballerina/jballerina.java;

function getCallStackTest() {
    runtime:StackFrame[] stackFrames = runtime:getStackTrace();
    assertEquality(stackFrames.length(), 3);
    assertEquality(stackFrames[0].toString(), "callableName: externGetStackTrace moduleName: ballerina.lang.runtime.0_0_1 fileName: runtime.bal lineNumber: 95");
    assertEquality(stackFrames[1].toString(), "callableName: getStackTrace moduleName: ballerina.lang.runtime.0_0_1 fileName: runtime.bal lineNumber: 85");
    assertEquality(stackFrames[2].toString(), "callableName: getCallStackTest  fileName: runtimelib_test.bal lineNumber: 21");

    java:StackFrameImpl stackFrame1 = <java:StackFrameImpl> stackFrames[1];
    string callableName = stackFrame1.callableName;
    string? moduleName = stackFrame1.moduleName;
    string fileName = stackFrame1.fileName;
    int lineNumber = stackFrame1.lineNumber;
    assertEquality(callableName, "getStackTrace");
    assertEquality(moduleName, "ballerina.lang.runtime.0_0_1");
    assertEquality(fileName, "runtime.bal");
    assertEquality(lineNumber, 85);

    java:StackFrameImpl stackFrame2 = <java:StackFrameImpl> stackFrames[2];
    callableName = stackFrame2.callableName;
    moduleName = stackFrame2.moduleName;
    fileName = stackFrame2.fileName;
    lineNumber = stackFrame2.lineNumber;
    assertEquality(callableName, "getCallStackTest");
    assertEquality(moduleName, ());
    assertEquality(fileName, "runtimelib_test.bal");
    assertEquality(lineNumber, 21);
}

function getCallStacktoStringTest() returns string[] {
    runtime:StackFrame[] stackFrames = runtime:getStackTrace();
    string[] output = [];
    foreach runtime:StackFrame stackFrame in stackFrames {
        output.push(stackFrame.toString());
    }
    return output;

}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}
