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

import AccessProject.module1;

public function testDefaultVisibility() {
    assertEquality(54, module1:getNumber());
}

public function testPublicVisibility() {
    assertEquality("Ballerina", module1:name);
}

public function testPublicVisibilityInComplexVar() {
    assertEquality(32, module1:byteValue);
    assertEquality(2.5, module1:floatValue);
    assertEquality(1001, module1:Id);
    assertEquality("John", module1:studentName);
    assertEquality(24, module1:studentDetail["Age"]);
    assertEquality("Paker", module1:studentDetail["surName"]);
    assertEquality("AssignmentFailed", module1:errorMsg);
    assertEquality("ArrayIndexOutOfBound", module1:errorCause);
    assertEquality(1, module1:riskLevel);
}

public function testPublicWithIsolatedFuncType() {
    assertEquality(10, module1:myIsolatedFunction());
}

public function testPublicWithIsolatedObjectType() {
    assertEquality(20, module1:myIsolatedObj.getVal());
}

public function testPublicFunctionWithRecordType() {
    module1:Foo foo = [{name: "Foo"}];
    assertEquality("Foo", module1:getFooName(foo));
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
