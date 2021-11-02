// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function testFunctionWithNeverReturnTypeReturningNil() returns never {
    string _ = "hello";
}

public function testNeverReturnFuncInvWithPanic() {
    panic unreached();
}

function unreached() returns never {
    panic error("error!!!");
}

function testCheckWithNeverTypeExpr() returns error? {
    any _ = check unreached();
    any _ = checkpanic unreached();
    return;
}

function testNeverTypeRequiredAndDefaultableParamInInv() {
   passIntValue(unreached(), unreached());
}

function passIntValue(int a, int b = 2) {

}

function num() returns error {
    return unreached();
}

function testInvalidUsageOfExprOfNeverInGroupedExpr() returns error? {
    error? x = check (unreached());
    return;
}
