// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function returnInt() returns int => 3;

function returnError() returns error? {
    return error("Error");
}

function testAssignmentNegative() {
    check returnInt() on fail e => error("Error occurred", e);
    string val1 = check returnInt() on fail e => error("Error occurred", e);
    int val2 = check returnError() on fail e => error("Error occurred", e);
}

function returnInt2() returns int|error => 3;

function testCheckOnFailWithBinaryExprNegative() {
    int val = check returnInt2() on fail e => error("Error occurred", e) + 2.0;
}

function testInvalidReturnType() {
    _ = check returnInt2() on fail e => returnInt();
    _ = check returnInt2() on fail e => returnInt2();
}
