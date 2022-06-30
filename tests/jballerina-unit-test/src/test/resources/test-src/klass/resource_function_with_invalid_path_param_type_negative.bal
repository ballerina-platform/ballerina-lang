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

string? stringNilArgument = ();

public isolated service class testClass {
    private int testField = 12;
    // only 'int', 'string', 'float', 'boolean', 'decimal' types are supported as path params, found 'other'
    // undefined module 'module1'
    // unknown type 'RequestMessage'
    resource function name [module1:RequestMessage a]() {

    }
}

public function testClientDeclarationError() {
    var errorClient = client object {
        resource function get path/[int b]/foo(string name2) returns int {
            return 1;
        }

        resource function get path/[int a]/foo/[string a](string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string name) returns int {
            return 1;
        }

        resource function get path/[int a]/foo2(string name, string address) {

        }

        resource function get path/[int... a]/[int... a]() returns int {
            return 1;
        }

        resource function post path/[string? c]/foo(string name2) returns string? {
            return stringNilArgument;
        }

        resource function path() returns int {
            return 1;
        }

        resource function get recordPath/[record {int a;}]() returns int {
            return 1;
        }

        resource function get recordPath2/[record {int a;} a]() returns int {
            return 1;
        }

        resource function get xmlPath/[xml... a]() returns int {
            return 1;
        }

        resource function get xmlPath2/[xml a]() returns int {
            return 1;
        }

        resource function get xmlPath2/[xml]() returns int {
            return 1;
        }

        resource function get mapPath/[map<string>]() returns int {
            return 1;
        }

        resource function get mapPath2/[map<string> a]() returns int {
            return 1;
        }

        resource function get intOrErrorPath/[int|error]() returns int {
            return 1;
        }

        resource function get intOrErrorPath2/[int|error a]() returns int {
            return 1;
        }

        resource function get errorPath/[error]() returns int {
            return 1;
        }

        resource function get errorPath2/[error a]() returns int {
            return 1;
        }

    };
}
