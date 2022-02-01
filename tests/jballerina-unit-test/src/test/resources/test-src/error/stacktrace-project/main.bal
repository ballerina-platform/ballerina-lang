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

import stacktrace_project.stacktrace as stacktrace;

public function testStackTraceElements() {
    error:StackFrame[] callStackElements = stacktrace:getStackTrace();

    assertEquality(callStackElements.length(), 2);
    assertEquality(callStackElements[0].toString(), "callableName: getStackTrace moduleName: " +
        "test_org.stacktrace_project.stacktrace.0 fileName: stacktrace.bal lineNumber: 18");
    assertEquality(callStackElements[1].toString(), "callableName: testStackTraceElements moduleName: " +
        "test_org.stacktrace_project.0 fileName: main.bal lineNumber: 20");

    error:StackFrame callStackElement = callStackElements[0];
}

function assertEquality(any|error actual, any|error expected) {
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
