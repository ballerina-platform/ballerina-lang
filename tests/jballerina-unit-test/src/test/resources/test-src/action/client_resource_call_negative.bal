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

type CustomString string;
type CustomString2 CustomString;

string? stringNilArgument = ();
() nilArgument = ();

CustomString customString = "x";
CustomString2 customString2 = customString;

const int intConstant = 1;
var varInt = 1;
var varBoolean = true;

public function testClientResourceFunctionCallError() {
    var successClient = client object {
        resource function get path() returns int {
            return 1;
        }

        resource function post path() returns int {
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
    };

    int _ = successClient->/path/[10*11]/foo(123);
    int _ = successClient->/path/[1]/foo("x", "y");
    int _ = successClient->/path/[1]/foo("x", "y", "z");
    int _ = successClient->/path/[1]/foo(nilArgument);
    int _ = successClient->/path/[1]/foo(stringNilArgument);

    int _ = successClient->/path/[1]/foo(name = 23);
    int _ = successClient->/path/[1]/foo(name = intConstant);
    int _ = successClient->/path/[1]/foo(name = varInt);
    int _ = successClient->/path/[1]/foo(name = varBoolean);
    int _ = successClient->/path/[1]/foo("arg", name = 23);
    int _ = successClient->/path/[1]/foo(name = 23, name = 23);
    int _ = successClient->/path/[1]/foo();
    int _ = successClient->/path/[1]/foo(b = 23);
    int _ = successClient->/path/[1]/foo2(address = 23, "name");
    int _ = successClient->/path.post("a");
    int _ = successClient->/path/[1]/foo2(name = customString, 23);
    int _ = successClient->/path/[1]/foo(customString, name = 23);
    int _ = successClient->/path/[1]/foo(customString, customString);
    int _ = successClient->/path/[1]/foo(customString, customString, customString);
    int _ = successClient->/path.post(customString);
}

public function testClientResourceFunctionCallAmbiguousError() {
    var successClient = client object {
        resource function get [string pathVar]/path() returns int {
            return 1;
        }

        resource function get foo/path() returns int {
            return 1;
        }

        resource function get foo/bar/path() returns int {
            return 1;
        }

        resource function get foo2/["Path" pathValue]() returns int {
            return 1;
        }

        resource function get foo2/path() returns int {
            return 1;
        }
    };

    int _ = successClient->/foo/path();
    int _ = successClient->/foo2/path();
    int _ = successClient->/foo2/["path"]();
}

public function testResourceCallWithErrorClient() {
    var errorClient = object {
        resource function get path/[string]() returns int {
            return 1;
        }
    };
    int _ = errorClient->/path/["a"];
}
