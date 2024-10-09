// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/lang.'int;

type Bar record {
    string sbar = "sBar";
};

type AnotherBar record {
    string sbar = "sBar";
    int x = 0;
};

type Status "ON"|"OFF";

type ON "ON";

public type Foo object {
    function test1(string aString, int anInt) returns string|error;

    public function test2(string aString);

    function test3(int anInt, string aString, string defaultable = "foo", int def2 = 10);

    function test4(string aString, 'int:Signed16 anInt, Bar... bars) returns string;

    function test5(Status... status) returns Bar;

    function test6([string, Status]... tup);

    function test7() returns Status;

    function test8('int:Signed16 anInt, Bar... bars) returns 'int:Signed16;
};

public class FooImpl1 {
    *Foo;

    // no error - param name mismatch is OK
    function test1(string str, int anInt) returns string|error {
        return "";
    }

    // visibility modifier mismatch
    function test2(string aString) {}

    // return type mismatch
    function test3(string aString, int anInt, string defaultable = "foo", int def2 = 10) returns string {
        return "";
    }

    // not assignable : param type
    function test4(string aString,int anInt, AnotherBar... bars) returns string {
        return "";
    }

    // no error - param name mismatch is OK
    function test5(Status... stat) returns Bar {
        return {};
    }

    // no error - param name mismatch is OK
    function test6([string, Status]... tupl) {}

    // param count mismatch
    function test7(int x = 0) returns Status {
        return "ON";
    }

    // not assignable : return type
    function test8('int:Signed16 anInt, Bar... bars) returns int {
        return 0;
    }
}

public type Timestamp readonly & object {
    public function toString() returns string;
};

readonly class InvalidReadOnlyClassWithMissingImpl {
    *Timestamp;

    public function toInt() returns int => 0;

    public function toFloat() returns float => 0;
}

readonly class InvalidReadOnlyClassWithMissingImpls {
    *InvalidReadOnlyClassWithMissingImpl;

    public function toInt() returns int => 0;
}

public type FooObj client object {
    isolated remote function execute(string aVar, int bVar);
    public function pause(string aVar, int bVar);
};

client class BarClass {
    *FooObj;

    public isolated function execute(string aVar, int bVar) {
    }

    remote function pause(string aVar, int bVar) {
    }
}

type BazObj client object {
    *FooObj;

    public isolated function execute(string aVar, int bVar);

    remote function pause(string aVar, int bVar);
};
