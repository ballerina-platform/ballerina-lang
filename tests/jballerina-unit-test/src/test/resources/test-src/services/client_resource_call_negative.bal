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

    int result = successClient->/path/[10*11]/foo(123);
    result = successClient->/path/[1]/foo("x", "y");
    result = successClient->/path/[1]/foo("x", "y", "z");
    result = successClient->/path/[1]/foo(nilArgument);
    result = successClient->/path/[1]/foo(stringNilArgument);

    result = successClient->/path/[1]/foo(name = 23);
    result = successClient->/path/[1]/foo(name = intConstant);
    result = successClient->/path/[1]/foo(name = varInt);
    result = successClient->/path/[1]/foo(name = varBoolean);
    result = successClient->/path/[1]/foo("arg", name = 23);
    result = successClient->/path/[1]/foo(name = 23, name = 23);
    result = successClient->/path/[1]/foo();
    result = successClient->/path/[1]/foo(b = 23);
    result = successClient->/path/[1]/foo2(address = 23, "name");
    result = successClient->/path.post("a");
    result = successClient->/path/[1]/foo2(name = customString, 23);
    result = successClient->/path/[1]/foo(customString, name = 23);
    result = successClient->/path/[1]/foo(customString, customString);
    result = successClient->/path/[1]/foo(customString, customString, customString);
    result = successClient->/path.post(customString);
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

    int result = successClient->/foo/path();
    result = successClient->/foo2/path();
    result = successClient->/foo2/["path"]();
}

public function testResourceCallWithErrorClient() {
    var errorClient = object {
        resource function get path/[string]() returns int {
            return 1;
        }
    };
    int result = errorClient->/path/["a"];
}
