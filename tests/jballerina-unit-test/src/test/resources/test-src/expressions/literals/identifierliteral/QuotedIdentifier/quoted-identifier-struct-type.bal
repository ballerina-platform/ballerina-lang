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

type Person record {
    string '1st_name;
    string '\ \/\:\@\[\`\{\~\u{2324}_last_name;
    int 'Ȧɢέ;
};

function useILWithinStruct() {
    Person person = {'1st_name: "Tom", '\ \/\:\@\[\`\{\~\u{2324}_last_name:"Hank", 'Ȧɢέ: 50};
    assertEquality("Tom",person.'1st_name);
    assertEquality("Hank",person.'\ \/\:\@\[\`\{\~\u{2324}_last_name);
    assertEquality(50,person.'Ȧɢέ);
}

function useILInStructVar() {
    Person 'ƮέŞŢ_Person_\ \/\:\@\[\`\{\~\u{2324} =
    {'1st_name: "Harry", '\ \/\:\@\[\`\{\~\u{2324}_last_name:"Potter", 'Ȧɢέ: 22};

    assertEquality("Harry",'ƮέŞŢ_Person_\ \/\:\@\[\`\{\~\u{2324}.'1st_name);
    assertEquality("Potter",'ƮέŞŢ_Person_\ \/\:\@\[\`\{\~\u{2324}.'\ \/\:\@\[\`\{\~\u{2324}_last_name);
    assertEquality(22,'ƮέŞŢ_Person_\ \/\:\@\[\`\{\~\u{2324}.'Ȧɢέ);
}

type '\ \/\:\@\[\`\{\~\u{2324}_123_ƮέŞŢ_Person record {
    string name;
};

function useILInStructName() {
    '\ \/\:\@\[\`\{\~\u{2324}_123_ƮέŞŢ_Person person = {name: "Jack"};
    assertEquality("Jack",person.name);
}

public function main() {
    useILWithinStruct();
    useILInStructVar();
    useILInStructName();
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
