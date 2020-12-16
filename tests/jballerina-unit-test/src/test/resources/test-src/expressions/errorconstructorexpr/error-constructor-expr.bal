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
    if (e.cause() is error) {
        assertEquals("Message2", (<error> e.cause()).message());
    } else if (e.cause() is ()) {
        assertEquals((), <()> e.cause());
    } else {
        panic error("Error cause should be error?");
    }
}

function errorConstructorExpr4() returns error {
    return error("Message1", error("Message2", error("Message3")));
}

function testErrorConstructorExpr4() {
    error e = errorConstructorExpr4();
    if (e.cause() is error) {
        e = <error> e.cause();
        if (e.cause() is error) {
            assertEquals("Message3", (<error> e.cause()).message());
        } else if (e.cause() is ()) {
            assertEquals((), <()> e.cause());
        } else {
            panic error("Error cause should be error?");
        }
    } else if (e.cause() is ()) {
        assertEquals((), <()> e.cause());
    } else {
        panic error("Error cause should be error?");
    }
}

function errorConstructorExpr5() returns error {
    return error("Message1", c = 200, d = 300);
}

function testErrorConstructorExpr5() {
    error e = errorConstructorExpr5();
    map<anydata|readonly> m = e.detail();
    int i1 = <int> m["c"];
    int i2 = <int> m["d"];
    assertEquals(i1, 200);
    assertEquals(i2, 300);
}

function errorConstructorExpr6() returns error {
    return error("Message1", error("Message2", a = 100, b = "400"), c = 200, d = 300);
}

function testErrorConstructorExpr6() {
    error e = errorConstructorExpr6();
    e = <error> e.cause();
    map<anydata|readonly> m = e.detail();
    int i1 = <int> m["a"];
    string i2 = <string> m["b"];
    int? i3 = <int?> m["c"];
    assertEquals(100, i1);
    assertEquals("400", i2);
    assertEquals((), i3);
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