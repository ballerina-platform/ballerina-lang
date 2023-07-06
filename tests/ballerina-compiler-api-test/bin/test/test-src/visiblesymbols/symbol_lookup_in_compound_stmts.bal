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

const c1 = "foo";
const c2 = 1;

function testRegularCompoundStmts() {
    any|error v = {x1: 1, b: true};

    match v {
        var {x1, b: x2} => {
            any|error a =
        }

        [var x3, var x4] => {
            any|error b =
        }

        error(var x5, val = var x6) => {
            any|error c =
        }

        var x7 if  => {
            any|error d =
        }

        {} => {

        }
    }

    foreach var i in 1...10 {
        float y = 1.2;

        while (true) {
            string z = "Z";

            lock {
                // cursor pos
            }
        }
        float z = 2.3;
        // cursor pos
    }

    do {
        string doStr = "test str in do";
    } on fail var e1 {
        string str = "test str in do on fail";
    }

    transaction {
        var res = commit;
    } on fail var e2 {
        string str = "test str in trx on fail";
    }

    retry {
        int y = 2;
    }

    retry transaction {
        string s = "";
    }
}
