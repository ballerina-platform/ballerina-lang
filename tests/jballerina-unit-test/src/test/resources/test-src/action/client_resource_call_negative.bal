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

int[2] integerParameterArray = [1, 2];
string[2] stringParameterArray = ["path1", "path2"];
boolean[2] booleanParameterArray = [true, false];

public function testClientResourceAccessArgumentsError() {
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

        resource function get path/foo(string name) returns int {
            return 1;
        }

        resource function get path/foo2(string name, string address) returns int {
            return 1;
        }

        resource function get path/foo3(string name = "abc") returns int {
            return 1;
        }

        resource function get path/foo4(string address, string name = "abc") returns int {
            return 1;
        }
    };

    int _ = successClient->/path/foo(123);
    int _ = successClient->/path/foo("x", "y");
    int _ = successClient->/path/foo("x", "y", "z");
    int _ = successClient->/path/foo(nilArgument);
    int _ = successClient->/path/foo(stringNilArgument);

    int _ = successClient->/path/foo(name = 23);
    int _ = successClient->/path/foo(name = intConstant);
    int _ = successClient->/path/foo(name = varInt);
    int _ = successClient->/path/foo(name = varBoolean);
    int _ = successClient->/path/foo("arg", name = 23);
    int _ = successClient->/path/foo(name = 23, name = 23);
    int _ = successClient->/path/foo();
    int _ = successClient->/path/foo(b = 23);
    int _ = successClient->/path/foo(customString, name = 23);
    int _ = successClient->/path/foo(customString, customString);
    int _ = successClient->/path/foo(customString, customString, customString);
    int _ = successClient->/path/foo2(address = 23, "name");
    int _ = successClient->/path/foo2(name = customString, 23);
    int _ = successClient->/path/foo3(name = 23);
    int _ = successClient->/path/foo3("name", name = "name2");
    int _ = successClient->/path/foo4(name = "name");
    int _ = successClient->/path/foo4("address", name = intConstant);
    int _ = successClient->/path/foo4("address", customString, "name2");
    int _ = successClient->/path/foo4(address = customString2, name = "name", key = "key");
    int _ = successClient->/path.post("a");
    int _ = successClient->/path.post(customString);
}

public function testAmbiguousResourceAccessError() {
    var successClient = client object {
        resource function get [string pathVar]/path() returns int {
            return 1;
        }

        resource function get foo/path() returns int {
            return 1;
        }

        resource function get foo2/["Path" pathValue]() returns int {
            return 1;
        }

        resource function get foo2/path() returns int {
            return 1;
        }

        resource function get foo3/[string ...]() returns int {
            return 1;
        }

        resource function get foo3/[string x]/[string y]() returns int {
            return 1;
        }

        resource function get foo4/[string ...]() returns int {
            return 1;
        }

        resource function get foo4/path() returns int {
            return 1;
        }

        resource function get foo5/[int ...]() returns int {
            return 1;
        }

        resource function get foo5/[int x]/[int y]() returns int {
            return 1;
        }

        resource function get foo6/[boolean ...]() returns int {
            return 1;
        }

        resource function get foo6/[boolean x]/[boolean y]() returns int {
            return 1;
        }
    };

    int _ = successClient->/foo/path();
    int _ = successClient->/foo2/path();
    int _ = successClient->/foo2/["path"]();
    int _ = successClient->/foo3/a/b();
    int _ = successClient->/foo3/[...stringParameterArray]();
    int _ = successClient->/foo4/path();
    int _ = successClient->/foo5/[5]/[6]();
    int _ = successClient->/foo5/[...integerParameterArray]();
    int _ = successClient->/foo6/[false]/[true]();
    int _ = successClient->/foo6/[...booleanParameterArray]();
}

public function testResourceCallWithErrorClient() {
    var errorClient = object {
        resource function get path/[string]() returns int {
            return 1;
        }
    };
    int _ = errorClient->/path/["a"];
}
