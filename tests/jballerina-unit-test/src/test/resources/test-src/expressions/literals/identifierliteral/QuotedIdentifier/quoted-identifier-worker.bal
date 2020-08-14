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

function useILAsWorkerName() {
     worker '\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Worker returns string {
         string 'var1_\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ = "sample result";
         return "this is a " + 'var1_\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ;
     }
     string result  = wait '\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Worker;
     assertEquality("this is a sample result", result);
}

function testWorker () returns int {
    worker '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker1 returns int {
        int result = 0;
        int i = 10;
        i -> '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker2;
        result = <- '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker2;
        return result;
    }

    worker '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker2 {
        int r = 120;
        int i = 0;
        i = <- '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker1;
        r = changeMessage(i);
        r -> '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker1;
    }
    return wait '\u{2324}\ \/\:\@\[\`\{\~_ƮέŞŢ_Worker1;
}

function changeMessage (int i) returns int {
    return i + 10;
}

function testWorkerInteractionWithIL() {
     int result  = testWorker();
     assertEquality(20, result);
}

public function main() {
    useILAsWorkerName();
    testWorkerInteractionWithIL();
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
