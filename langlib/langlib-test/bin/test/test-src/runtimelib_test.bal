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
import ballerina/lang.runtime as runtime;

function getCallStacktoStringTest() {
    runtime:StackFrame[] stackFrames = runtime:getStackTrace();
    assertEquality(stackFrames.length(), 3);
    assertEquality("callableName: externGetStackTrace moduleName: ballerina.lang.runtime.0 fileName: runtime.bal " +
                    "lineNumber: 121", stackFrames[0].toString());
    assertEquality("callableName: getStackTrace moduleName: ballerina.lang.runtime.0 fileName: runtime.bal " +
                    "lineNumber: 111", stackFrames[1].toString());
    assertEquality("callableName: getCallStacktoStringTest  fileName: runtimelib_test.bal lineNumber: 21",
                    stackFrames[2].toString());

    java:StackFrameImpl stackFrame1 = <java:StackFrameImpl> stackFrames[1];
    string callableName = stackFrame1.callableName;
    string? moduleName = stackFrame1.moduleName;
    string fileName = stackFrame1.fileName;
    int lineNumber = stackFrame1.lineNumber;

    assertEquality("getStackTrace", callableName);
    assertEquality("ballerina.lang.runtime.0", moduleName);
    assertEquality("runtime.bal", fileName);
    assertEquality(111, lineNumber);

    java:StackFrameImpl stackFrame2 = <java:StackFrameImpl> stackFrames[2];
    callableName = stackFrame2.callableName;
    moduleName = stackFrame2.moduleName;
    fileName = stackFrame2.fileName;
    lineNumber = stackFrame2.lineNumber;

    assertEquality("getCallStacktoStringTest", callableName);
    assertEquality((), moduleName);
    assertEquality("runtimelib_test.bal", fileName);
    assertEquality(21, lineNumber);
}

function testSleepDecimalValue() {
    decimal delay = 2.5;
    runtime:sleep(delay);

    delay = -1321930131998892321.123;
    runtime:sleep(delay);

    delay = 0.0;
    runtime:sleep(delay);
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
