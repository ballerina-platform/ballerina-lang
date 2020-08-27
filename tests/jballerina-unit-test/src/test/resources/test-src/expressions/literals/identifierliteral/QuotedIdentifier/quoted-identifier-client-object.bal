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

public type '\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Connector client object {
    boolean action2Invoked = false;

    public function init(string param1, string param2, int param3) {

    }

    public remote function action1() returns string {
        string 'sample_String_1 = "this ";
        string '\ \/\:\@\[\`\{\~\u{2324}_var = "is ";
        string 'üňĩćőđę_var = "action 1";
        return 'sample_String_1 + '\ \/\:\@\[\`\{\~\u{2324}_var + 'üňĩćőđę_var;
    }

    public remote function '\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Action() returns string {
        string 'sample_String_2 = "this ";
        string '\ \/\:\@\[\`\{\~\u{2324}_var2 = "is ";
        string 'üňĩćőđę_var2 = "action 2";
        return 'sample_String_2 + '\ \/\:\@\[\`\{\~\u{2324}_var2 + 'üňĩćőđę_var2;
    }
};

function testConnectorNameWithIL() {
    '\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Connector testConnector = new("MyParam1", "MyParam2", 5);
    string value = testConnector->action1();
    assertEquality("this is action 1", value);
}

function testConnectorActionWithIL() {
    '\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Connector testConnector = new("MyParam1", "MyParam2", 5);
    string value = testConnector->'\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ_Action();
    assertEquality("this is action 2", value);
}

public function main() {
    testConnectorNameWithIL();
    testConnectorActionWithIL();
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
