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

function testConditioanlStmt() {
    func(1);

    int a = bar();

    ARecord aRecord = {a: 2};

    if b.length() == 1 {
        func(2);
    }

    if bar() > 2 {
        b = b + "B";
    } else if bar() < 2 {
        func(1);
    } else {
        func(2);
    }

    if aRecord.a > 2 {
        aRecord.a = 3;
    }
}

string b = "B";

function func(int x) {
}

function bar() returns int {
    return 3;
}

type ARecord record {|
    int a;
|};
