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

type MyClientObjectType client object {
    resource function get foo/[int b]/[string... a]() returns int;
    function name();
    resource function get boo/[int b]/[int... a](string c) returns boolean;
    resource function post boo/[byte b]/[byte... a](string c) returns int;
    resource function post [1 b]/[2... a](string c) returns int;
};

function testResourceMethodAssignability() {
    client object {
        resource function get foo/[int a]/[string...]() returns int;
    } objectVar1 = client object {
        resource function get foo/[int a]/[string...]() returns int {
            return a;
        }
    };

    int result = objectVar1->/foo/[5];
    assertEquality(result, 5);

    MyClientObjectType objectVar2 = client object {
        resource function get foo/[int b]/[string... a]() returns int {
            return b;
        }

        resource function get boo/[int b]/[int... a](string c) returns boolean {
            if (a.length() > 0) {
                return true;
            }
            return false;
        }

        function name() {
        }

        resource function post boo/[int b]/[int... a](string c) returns int {
            return b;
        }

        resource function post [int b]/[int... a](string c) returns int {
            return b;
        }
    };

    int result2 = objectVar2->/foo/[5]/book;
    assertEquality(result2, 5);

    boolean result3 = objectVar2->/boo/[5]("hellow");
    assertEquality(result3, false);

    boolean result4 = objectVar2->/boo/[...[1, 2]]("hellow");
    assertEquality(result4, true);

    int result5 = objectVar2->/boo/[5]/[6].post("c");
    assertEquality(result5, 5);

    int result6 = objectVar2->/[1]/[2].post("c");
    assertEquality(result6, 1);

    client object {
        resource function get boo/[int b]/[int... a](string c) returns boolean;
    } objectVar3 = objectVar2;

    boolean result7 = objectVar3->/boo/[1]/[2].get("c");
    assertEquality(result7, true);

    client object {
        resource function get foo/[int b]/["book"... a]() returns int;
    } objectVar4 = objectVar2;

    int result8 = objectVar4->/foo/[2]();
    assertEquality(result8, 2);

    client object {
        function name();
    } objectVar5 = objectVar2;

    () result9 = objectVar5.name();
    assertEquality(result9, ());

    client object {
        *MyClientObjectType;
    } objectVar6 = objectVar2;

    int result10 = objectVar6->/[1]/[2].post("c");
    assertEquality(result10, 1);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata actual, anydata expected) {
    if (actual == expected) {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
