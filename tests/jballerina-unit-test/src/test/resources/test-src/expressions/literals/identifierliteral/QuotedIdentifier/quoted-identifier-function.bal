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

const ASSERTION_ERR_REASON = "AssertionError";

function '\ \/\:\@\[\`\{\~\u{2324}_123_ƮέŞŢ_IL_function(string val) returns string {
    string s = " with IL function name";
    return val + s;
}

function testFunctionNameWithIL() {
     assertEquality("test with IL function name", '\ \/\:\@\[\`\{\~\u{2324}_123_ƮέŞŢ_IL_function("test"));
}

function passILValuesToFunction() {
    string '1st_Name = "Bill";
    string '\ \/\:\@\[\`\{\~\u{2324}_last_name = "Kary";
    int 'Ȧɢέ = 40;

    string expected = "first name :Bill, last name :Kary, age :40";
    assertEquality(expected,passILValuesAsParams('1st_Name, '\ \/\:\@\[\`\{\~\u{2324}_last_name, 'Ȧɢέ));
}

function passILValuesAsParams(string '1st_Arg, string '\ \/\:\@\[\`\{\~\u{2324}_arg2, int 'üňĩćőđę_arg3)
returns string {
    string result = "first name :" + '1st_Arg +
    ", last name :" + '\ \/\:\@\[\`\{\~\u{2324}_arg2 +
    ", age :" + 'üňĩćőđę_arg3.toString();
    return result;
}

public function main() {
    testFunctionNameWithIL();
    passILValuesToFunction();
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

