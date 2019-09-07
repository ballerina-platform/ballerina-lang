// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Foo record {
    string s;
    int i?;
};

float f = 1.0;
string s = "not s";

function getInt() returns int {
    return 1;
}

function getString(string s) returns string {
    return s;
}

function testInvalidExprAsRecordKey() {
    Foo f1 = { s: "str", [f]: 1.0 };
    Foo f2 = { [getString("s")]: "str" };
    Foo f3 = { s: "str", [getInt()]: 1 };
    Foo f4 = { s: "str", [getString(true)]: 1 };
    Foo f5 = { [s]: "str" };

    error e = error("test error");
    Foo f6 = { s: "str", [getString("e")]: e };
}
