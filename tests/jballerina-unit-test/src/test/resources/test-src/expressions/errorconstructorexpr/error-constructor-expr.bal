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
import ballerina/lang.'value;

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
    map<value:Cloneable> m = e.detail();
    assertEquals(<int> checkpanic m["c"], 200);
    assertEquals(<int> checkpanic m["d"], 300);
}

function errorConstructorExpr6() returns error {
    return error("Message1", error("Message2", a = 100, b = "400"), c = 200, d = 300);
}

function testErrorConstructorExpr6() {
    error e = errorConstructorExpr6();
    e = <error> e.cause();
    map<value:Cloneable> m = e.detail();
    assertEquals(100, <int> checkpanic m["a"]);
    assertEquals("400", <string> checkpanic m["b"]);
    assertEquals((), <int?> checkpanic m["c"]);
}

type MyError2 error <map<string>>;

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

type AppError distinct error<record { int code; }>;
type CodeError distinct error<record { int no; }>;
type AppCodeError AppError & CodeError;
type MyError error<record {| byte code; |}>;

function testContextuallyExpectedErrorCtor() {
    AppError e = error("message", code = 2);
    error eDash = e;
    assertEquals(eDash is AppError, true);

    AppCodeError ace = error("message", code = 3, no = 1);
    assertEquals(ace.message(), "message");
    assertEquals(ace.detail().code, 3);
    assertEquals(ace.detail().no, 1);

    var eTuple = getError(error("message", code = 22));
    assertEquals(eTuple[0].detail().code, 22);
    assertEquals(eTuple[1].detail().no, 2);
    assertEquals(eTuple[2][0].detail().code, 0);
    assertEquals(eTuple[2][0].detail().no, 0);

    // Test picking correct type from a union
    AppError|CodeError|AppCodeError|int|map<string> u = error("msg", code = 100);
    assertEquals(u is AppError, true);
    if (u is AppError) {
        assertEquals(u.detail().code, 100);
    }

    u = error("msg", no = 100);
    assertEquals(u is CodeError, true);
    if (u is CodeError) {
        assertEquals(u.detail().no, 100);
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/29345
    //u = error("msg", code = 100, no = 100);
    //assertEquals(u is AppCodeError, true);
    //if (u is AppCodeError) {
    //    assertEquals(u.detail().code, 100);
    //    assertEquals(u.detail().no, 100);
    //}

    readonly r = error("msg", code = 100, no = 100);
    if (r is error) {
        var code = r.detail()["code"];
        var no = r.detail()["no"];
        if (code is anydata && no is anydata) {
            assertEquals(code, 100);
            assertEquals(no, 100);
        } else {
            panic error("Invalid State");
        }
    } else {
        panic error("Invalid state: expected `error` found + `" + (typeof r).toString() + "`");
    }

    MyError x = error MyError("error!", code = 1);
    anydata code = x.detail().code;
    if !(code is byte) {
        panic error("Expected byte, found: " + (typeof code).toString());
    }
}

function getError(AppError ae) returns [AppError, CodeError, AppCodeError[]] {
    return [ae, error("msg", no = 2), [error("m", code = 0, no = 0)]];
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
