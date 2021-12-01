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

boolean w = true;
int x = 1;
function foo() returns int => 1;

type Foo record {|
    int i = x;
    int j = foo();
    Bar k = new;
|};

function testInvalidNonIsolatedRecordDefaultValues() {
    int y = 100;

    record {
        int i = y;
        string j = bar(1);
        string k = bar(x);
        object {
            boolean b;

            function getB() returns boolean;
        } ob = object {
            boolean b = w;

            isolated function getB() returns boolean => self.b;
        };
    } _ = {"oth": 2.34};
}

function (int i) returns string bar = function (int i) returns string {
    if i == 1 {
        return "a";
    }

    return "oth";
};

class Bar {
    int i;

    function init() { // not marked `isolated`
        self.i = 1;
    }
}
