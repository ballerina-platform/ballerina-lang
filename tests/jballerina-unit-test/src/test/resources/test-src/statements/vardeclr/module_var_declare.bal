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

int i;
string s;
int a;
int b;
error er;

const ERROR_REASON = "Error Reason";
const ASSERTION_ERROR_REASON = "AssertionError";

function __init() {
    i = 10;
    s = "Test string";
    int x = 2;
    a = x + 10;
    b = 31 + foo();
    er = error(ERROR_REASON, message = "error message");
}

function foo() returns int {
    return 1;
}

function testModuleVarDeclaration() {
    if (i == 10 && s == "Test string" && a == 12 && b == 32) {
        return;
    }

    string? msg = er.detail()?.message;
    if (msg is string && <string> msg == "error message") {
        return;
    }

    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found 'false'");
}
