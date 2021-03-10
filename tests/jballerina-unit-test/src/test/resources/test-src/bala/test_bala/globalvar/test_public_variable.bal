//  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import testorg/publicVariables;

public function testDefaultVisibility() {
    assertEquality(54, publicVariables:getNumber());
}

public function testPublicVisibility() {
    assertEquality("Ballerina", publicVariables:name);
}

public function testPublicVisibilityInComplexVar() {
    assertEquality(32, publicVariables:byteValue);
    assertEquality(2.5, publicVariables:floatValue);
    assertEquality(1001, publicVariables:Id);
    assertEquality("John", publicVariables:studentName);
    assertEquality(24, publicVariables:studentDetail["Age"]);
    assertEquality("Paker", publicVariables:studentDetail["surName"]);
    assertEquality("AssignmentFailed", publicVariables:errorMsg);
    assertEquality("ArrayIndexOutOfBound", publicVariables:errorCause);
    assertEquality(1, publicVariables:riskLevel);
}

public function testPublicWithIsolatedFuncType() {
    assertEquality(10, publicVariables:myIsolatedFunction());
}

public function testPublicWithIsolatedObjectType() {
    assertEquality(20, publicVariables:myIsolatedObj.getVal());
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
