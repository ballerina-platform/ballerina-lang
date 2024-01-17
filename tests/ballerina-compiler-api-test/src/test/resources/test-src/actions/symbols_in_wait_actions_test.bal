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

function testSingleWaitAction() returns error? {
    string name = "John";
    int age = 5;

    worker W1 returns int {
        return age;
    }

    any result = check wait foo(name, b = age);
    int intResult = wait W1;
}

function testMultipleWaitAction() returns error? {
    worker WA returns string|error {
        return "sam";
    }
    worker WB returns string|error {
        return "cal";
    }

    worker WC returns string|error {
        return "sam";
    }
    worker WD returns string|error {
        return "cal";
    }

    map<string|error|string|error> result1 = wait {a: WA, b: WB};
    map<string|error|string|error> result2 = wait {WC, WD};
    record {|string|error a; string|error b;|} result3 = wait {a: WA, b: WB};
}

function testAlternateWaitAction() {
    worker WA returns int {
        return 1;
    }

    worker WB returns string {
        return "one";
    }

    int|string result = wait WA | WB;
}

function foo(string a, int b, any... other) returns future {
}
