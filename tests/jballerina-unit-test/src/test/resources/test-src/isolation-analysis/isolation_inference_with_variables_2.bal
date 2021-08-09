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

int i = 0;

function functionAccessingIsolatedVarWithInferredFunctionAsTransferExpr() returns int[] {
    lock {
        i = 1;
        return f1();
    }
}

function f1() returns int[] {
    if 0 > 1 {
        _ = functionAccessingIsolatedVarWithInferredFunctionAsTransferExpr();
    }

    return [];
}

public function testIsolatedInference() {
    assertTrue(<any> functionAccessingIsolatedVarWithInferredFunctionAsTransferExpr is isolated function);
    assertTrue(<any> f1 is isolated function);
}

isolated function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

isolated function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
