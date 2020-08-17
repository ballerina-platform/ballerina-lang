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

//initialize final variables
final float 'const_IL_123 = 77.80;
final float '\ \/\:\@\[\`\{\~\u{2324}_IL = 88.90;
final float 'üňĩćőđę_ƈȏɳʂʈ_IL = 99.10;

//initialize global variables
string 'global_var_123 = "IL with global var";
string '\ \/\:\@\[\`\{\~\u{2324}_global_var = "IL with special characters in global var";
string 'üňĩćőđę_ĠĿŐΒȂɭ_var = "IL with unicode characters in global var";


function testFinalVariableIL() {
    assertEquality(77.80,'const_IL_123);
    assertEquality(88.90,'\ \/\:\@\[\`\{\~\u{2324}_IL);
    assertEquality(99.10,'üňĩćőđę_ƈȏɳʂʈ_IL);
}

function testGlobalVariableIL() {
    assertEquality("IL with global var",'global_var_123 );
    assertEquality("IL with special characters in global var",'\ \/\:\@\[\`\{\~\u{2324}_global_var);
    assertEquality("IL with unicode characters in global var",'üňĩćőđę_ĠĿŐΒȂɭ_var);
}

function testLocalVariableIL() {
    string 'local_var_123 = 'global_var_123;
    string '\ \/\:\@\[\`\{\~\u{2324}_local_var = '\ \/\:\@\[\`\{\~\u{2324}_global_var;
    string 'üňĩćőđę_ɬȭςαʆ_var = "IL with unicode characters in local var";
    assertEquality("IL with global var",'local_var_123 );
    assertEquality("IL with special characters in global var",'\ \/\:\@\[\`\{\~\u{2324}_local_var);
    assertEquality("IL with unicode characters in local var",'üňĩćőđę_ɬȭςαʆ_var);
}


public function main() {
    testFinalVariableIL();
    testGlobalVariableIL();
    testLocalVariableIL();
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
