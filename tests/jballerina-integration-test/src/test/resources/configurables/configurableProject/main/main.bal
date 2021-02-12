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
import ballerina/test;
import ballerina/jballerina.java;

configurable int intVar = 5;
configurable float floatVar = 9.5;
configurable string stringVar = ?;
configurable boolean booleanVar = ?;
configurable decimal decimalVar = 10.1;

configurable int[] & readonly intArr = ?;
configurable float[] & readonly floatArr = [9.0, 5.3, 5.6];
configurable string[] & readonly stringArr = ["apple", "orange", "banana"];
configurable boolean[] & readonly booleanArr = [true, true];
configurable decimal[] & readonly decimalArr = ?;

type AuthInfo record {|
    readonly string username;
    int id = 100;
    string password;
    string[] scopes?;
    boolean isAdmin = false;
|};

type UserTable table<AuthInfo> key(username);
type nonKeyTable table<AuthInfo> ;

configurable AuthInfo & readonly admin = ?;
configurable UserTable & readonly users = ?;
configurable nonKeyTable & readonly nonKeyUsers = ?;

public function main() {
    testSimpleValues();
    testArrayValues();
    testRecordValues();
    testTableValues();

    print("Tests passed");
}

function testSimpleValues() {
    test:assertEquals(42, intVar);
    test:assertEquals(3.5, floatVar);
    test:assertEquals("abc", stringVar);
    test:assertTrue(booleanVar);

    decimal result = 24.87;
    test:assertEquals(result, decimalVar);
}

function testArrayValues() {
    test:assertEquals([1, 2, 3], intArr);
    test:assertEquals([9.0, 5.6], floatArr);
    test:assertEquals(["red", "yellow", "green"], stringArr);
    test:assertEquals([true, false, false, true], booleanArr);

    decimal[] & readonly resultArr = [8.9, 4.5, 6.2];
    test:assertEquals(resultArr, decimalArr);
}

function testRecordValues() {
    test:assertEquals("jack", admin.username);
    test:assertEquals(100, admin.id);
    test:assertEquals("password", admin.password);
    test:assertEquals(["write", "read", "execute"], admin["scopes"]);
    test:assertTrue(admin.isAdmin);
}

function testTableValues() {
    
    test:assertEquals(3, users.length());
    test:assertEquals(3, nonKeyUsers.length());

    AuthInfo user1 = {
        username: "alice",
        id: 11,
        password: "password1",
        scopes: ["write"]
    };

    AuthInfo user2 = {
        username: "bob",
        id: 22,
        password: "password2",
        scopes: ["write", "read"]
    };

    AuthInfo user3 = {
        username: "john",
        id: 33,
        password: "password3"
    };

    test:assertEquals(user1, users.get("alice"));
    test:assertEquals(user2, users.get("bob"));
    test:assertEquals(user3, users.get("john"));
}

//Extern methods to verify no errors while testing
function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
