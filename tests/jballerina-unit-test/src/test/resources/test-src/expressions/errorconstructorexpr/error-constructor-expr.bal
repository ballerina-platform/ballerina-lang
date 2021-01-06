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

const MSG = "Message";

function errorConstructorExpr1() returns error {
    return error("Message");
}

function testErrorConstructorExpr1() {
    error e = errorConstructorExpr1();
    assertEquals("Message", e.message());
}

function errorConstructorExpr2() returns error {
    return error("Message");
}

function testErrorConstructorExpr2() {
    error e = errorConstructorExpr2();
    assertEquals(MSG, e.message());
}

function errorConstructorExpr3() returns error {
    return error("Message1", error("Message2"));
}

function testErrorConstructorExpr3() {
    error e = errorConstructorExpr3();
    assertEquals("Message1", e.message());
    error? eCause = e.cause();
    if (eCause is error) {
        assertEquals("Message2", eCause.message());
    } else {
        assertEquals((), eCause);
    }
}

function errorConstructorExpr4() returns error {
    return error("Message1", error("Message2", error("Message3")));
}

function testErrorConstructorExpr4() {
    error e = errorConstructorExpr4();
    error? eCause = e.cause();
    if (eCause is error) {
        error? erCause = <error> eCause.cause();
        if (erCause is error) {
            assertEquals("Message3", erCause.message());
        } else {
            assertEquals((), erCause);
        }
    } else {
        assertEquals((), eCause);
    }
}

function errorConstructorExpr5() returns error {
    return error("Message1", c = 200, d = 300);
}

function testErrorConstructorExpr5() {
    error e = errorConstructorExpr5();
    map<anydata|readonly> m = e.detail();
    assertEquals(<int> checkpanic m["c"], 200);
    assertEquals(<int> checkpanic m["d"], 300);
}

function errorConstructorExpr6() returns error {
    return error("Message1", error("Message2", a = 100, b = "400"), c = 200, d = 300);
}

function testErrorConstructorExpr6() {
    error e = errorConstructorExpr6();
    e = <error> e.cause();
    map<anydata|readonly> m = e.detail();
    assertEquals(100, <int> checkpanic m["a"]);
    assertEquals("400", <string> checkpanic m["b"]);
    assertEquals((), <int?> checkpanic m["c"]);
}

type MyError1 error <*>;
type MyError2 error <map<string>>;

function errorConstructorExpr7() returns error {
    return error MyError1("Message1");
}

function testErrorConstructorExpr7() {
    assertEquals("Message1", errorConstructorExpr7().message());
}

function errorConstructorExpr8() returns MyError2 {
    return error MyError2("Message1", a = "str1", b = "str2");
}

function testErrorConstructorExpr8() {
    MyError2 e = errorConstructorExpr8();
    string s1 = <string> e.detail()["a"];
    string s2 = <string> e.detail()["b"];
    assertEquals("str1", s1);
    assertEquals("str2", s2);
}

function errorConstructorExpr9() returns error {
    return error("Message", ());
}

function testErrorConstructorExpr9() {
    error e = errorConstructorExpr9();
    assertEquals(true, e.cause() is ());
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}