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
public client class MyClient {
    resource function get .() returns int {
        return 3;
    }

    resource function post .() returns int {
        return 2;
    }

    resource function get books/names() returns string[2] {
        return ["book1", "book2"];
    }

    function get() returns int {
        return 0;
    }
}

public client class MyClient2 {
    resource function get books/names() returns string[2] {
        return ["book1", "book2"];
    }

    resource function get books/[int id]() returns string {
        return "book" + id.toString();
    }

    resource function get books/[string name]/[float volume]() returns string {
        return name + "." + volume.toString();
    }

    resource function post books/[string name]/[decimal volume]() returns string {
        return name + "." + volume.toString();
    }

    resource function get [boolean flag]() returns boolean {
        return flag;
    }
}

public const PATH = "someLongPathSegment";

public type X "x";

public client class MyClient3 {
    resource function get books/[PATH]() returns int {
        return 1;
    }

    resource function post books/[X]() returns int {
        return 2;
    }

    resource function put books/[PATH](int a) returns int {
        return a;
    }

    resource function someOtherMethod books/[PATH...](int a) returns int {
        return a;
    }
}

public client class MyClient4 {
    resource function get books/[string... a]() returns string[] {
        return a;
    }

    resource function get [int a]/[int... b]() returns int[] {
        return b;
    }

    resource function post [int a]/[int b]() returns int {
        return b;
    }

    resource function post books/["books"... a]() returns string[] {
        return a;
    }

    resource function put books/["books" a]() returns string {
        return a;
    }

    resource function someOtherMethod books/["books" a]/["books"... b]() returns string[] {
        return b;
    }

    resource function someOtherMethod2 books/books/["books" a]/["books"... b]() returns string[] {
        return b;
    }

    resource function someOtherMethod3 books/books/["books"... b]() returns string[] {
        return b;
    }
}

public client class MyClient5 {
    resource function get [string... a](string... b) returns string[][] {
        return [a, b];
    }

    resource function get1 [string](int a, int b) returns int {
        return a;
    }

    resource function get path/[int a]/path2/[string](string b, int... c) returns [int, string, int[]] {
        return [a, b, c];
    }

    resource function put [int](int a, int b = 5) returns int[2] {
        return [a, b];
    }
}

public client class MyClient6 {
    resource function get [string... a](string... b) returns string[][] {
        return [a, b];
    }

    resource function get1 [string](int a, int b) returns int? {
        return a;
    }

    resource function get path/[int a]/path2/[string](string b, int... c) returns [int, string, int[]] {
        return [a, b, c];
    }

    resource function put [int](int a, int b = 5) returns int[2] {
        return [a, b];
    }

    resource function get path/[boolean a](string b) returns boolean|xml {
        return a;
    }

    resource function get [float a](string b) returns float|map<int> {
        return a;
    }

    resource function get [decimal a]/path(string b) returns decimal {
        return a;
    }
}

public type MyBbjectType client object {
    resource function get [string... a](string... b) returns string[][];
    resource function get1 [string](int a, int b) returns int;
    resource function get path/[int a]/path2/[string](string b, int... c) returns [int, string, int[]];
    resource function put [int](int a, int b = 5) returns int[2];
    resource function put [PATH]/path(int a) returns int;
};

public MyBbjectType myClinet = client object {
    resource function get [string... a](string... b) returns string[][] {
        return [a, b];
    }

    resource function get1 [string](int a, int b) returns int {
        return a;
    }

    resource function get path/[int a]/path2/[string](string b, int... c) returns [int, string, int[]] {
        return [a, b, c];
    }

    resource function put [int](int a, int b = 5) returns int[2] {
        return [a, b];
    }

    resource function put [PATH]/path(int a) returns int {
        return a;
    }
};

public client class MyClient7 {
    resource isolated function get customers\-json() returns string {
        return "response1";
    }

    resource isolated function get '955() returns string {
        return "response2";
    }

    resource isolated function get A\u{0042}() returns string {
        return "response3";
    }

    resource isolated function post AB() returns string {
        return "response4";
    }
}
