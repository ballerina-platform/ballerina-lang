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

int x = 10;

function testObjectConstructor() {
    int y = 0;

    var obj = object {
        string name = "";
        int age;

        function init() {
            self.age = 0;
        }

        function getName() returns string {
            return self.name;
        }
    };
}

function testAnonFuncs() {
    int y = 10;

    var fn1 = function (string s) {
        int z = y + 1;
    };

    var fn2 = function (string s) returns int => y + 1;

    function (int) returns int fn3 = (p) => p + y;
}

function testMiscExprs() {
    int b = let int p = 2, int z = 5+p in z * p * x;

    string strTemp = string `a string template: ${x}`;

    'object:RawTemplate rawTemp = `a raw template: ${b}`;

    b = 10 + let int y = 20 in ;

    function (int) returns int fn = (p) => let int y = 20 in ;

    b = true ? (let int z = 0 in z) : (let int y = 1 in y);
}

// utils
type Person record {|
    readonly int id;
    string name;
|};
