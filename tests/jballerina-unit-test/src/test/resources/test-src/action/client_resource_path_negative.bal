// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const string stringConstant = "String Constant";
const int intConstant = 1;

var varString = "String var";
var varInt = 1;
var varBoolean = true;

string[] stringPathParameters = ["foo", "bar"];
string[] stringPathParameters2 = ["foo", "bar", "foo2"];
string[] stringPathParameters3 = ["foo"];
int[] intPathParameters = [1, 2, 3];

int? intNilArgument = ();
() nilArgument = ();
int? intOrNilArgument = 3;

record {int a;} recordValue = {a: 3};

public function testUndefineResourcePath() {
    var successClient = client object {
        resource function get path() returns int {
            return 1;
        }

        resource function get path/[int a]() returns int {
            return 1;
        }

        resource function get path/[int a]/foo(string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string address) returns int {
            return 1;
        }

        resource function get path/foo/bar() {

        }

        resource function get stringPath/[string]() returns int {
            return 1;
        }

        resource function get intQuotedPath/'5() returns int {
            return 1;
        }

        resource function get intPath/[int]() returns int {
            return 1;
        }
    };

    int _ = successClient->/path2.post();
    int _ = successClient->/path.put();
    int _ = successClient->/path/[...intPathParameters];
    int _ = successClient->/path/[...stringPathParameters3]/bar;
    int _ = successClient->/path/[stringPathParameters];
    int _ = successClient->/path/[...stringPathParameters];
    int _ = successClient->/path/[...stringPathParameters2];
    int _ = successClient->/path/["abc"];
    int _ = successClient->/path/[varString];
    int _ = successClient->/path/[varBoolean];
    int _ = successClient->/path/[stringConstant];
    int _ = successClient->/stringPath/[recordValue.a];
    int _ = successClient->/stringPath/[intConstant];
    int _ = successClient->/stringPath/[varBoolean];
    int _ = successClient->/stringPath/[varInt];
    int _ = successClient->/intQuotedPath/[5];
    int _ = successClient->/intPath/'5;
    int _ = successClient->/intPath/foo/foo2/bar/bar2/foo3;
    int _ = successClient->/path/[intNilArgument];
    int _ = successClient->/path/[nilArgument];
    int _ = successClient->/path/[getIntOrNilArgument()];
}

function getIntOrNilArgument() returns int? {
    return intOrNilArgument;
}
