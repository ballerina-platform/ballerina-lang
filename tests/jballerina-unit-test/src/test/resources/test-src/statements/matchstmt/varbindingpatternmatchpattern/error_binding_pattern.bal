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

import ballerina/lang.value;

const MSG = "Const Message";

function errorBindingPattern1(any|error e) returns string {
    match e {
        var error(_) => {
            return "match1";
        }
    }
    return "No match";
}

function testErrorBindingPattern1() {
    assertEquals("match1", errorBindingPattern1(error("Message")));
    assertEquals("match1", errorBindingPattern1(error(MSG)));
    assertEquals("No match", errorBindingPattern1("Message1"));
}

function errorBindingPattern2(any|error e) returns string {
    match e {
        var error(a) => {
            return <string> a;
        }
    }
    return "No match";
}

function testErrorBindingPattern2() {
    assertEquals("Message", errorBindingPattern2(error("Message")));
    assertEquals(MSG, errorBindingPattern2(error(MSG)));
    assertEquals("No match", errorBindingPattern2("Message1"));
}

function errorBindingPattern3(error e) returns string {
    match e {
        var error(a) if a is string => {
            return a;
        }
    }
    return "No match";
}

function testErrorBindingPattern3() {
    assertEquals("Message", errorBindingPattern3(error("Message")));
}

function errorBindingPattern4(error e) returns string {
    match e {
        var error(a, error(b)) => {
            return "match1";
        }
        var error(a) => {
            return "match2";
        }
    }
    return "No match";
}

function testErrorBindingPattern4() {
    assertEquals("match1", errorBindingPattern4(error("Message1", error("Message2"))));
    assertEquals("match2", errorBindingPattern4(error("Message1")));
}

function errorBindingPattern5(error e) returns string {
    match e {
        var error(a, b) if b is error => {
            return b.message();
        }
    }
    return "No match";
}

function testErrorBindingPattern5() {
    assertEquals("Message2", errorBindingPattern5(error("Message1", error("Message2"))));
    assertEquals("No match", errorBindingPattern5(error("Message1")));
}

function errorBindingPattern6(error e) returns string {
    match e {
        var error(a, b) if b is error && b.message() == "Old Message" => {
            return "match";
        }
        var error(a, b) => {
            return (<error> b).message();
        }
    }
    return "No match";
}

function testErrorBindingPattern6() {
    assertEquals("Message2", errorBindingPattern6(error("Message1", error("Message2"))));
    assertEquals("match", errorBindingPattern6(error("Message1", error("Old Message"))));
}

function errorBindingPattern7(error e) returns string {
    match e {
        var error(a, error(b), c1=c) => {
            return "match1";
        }
    }
    return "No match";
}

function testErrorBindingPattern7() {
    assertEquals("match1", errorBindingPattern7(error("Message1", error("Message2"), c1=200)));
    assertEquals("match1", errorBindingPattern7(error("Message1", error("Message2"), c1=2)));
    assertEquals("match1", errorBindingPattern7(error("Message1", error("Message2"), c1=4)));
    assertEquals("No match", errorBindingPattern7(error("Message1")));
}

type MyError1 error<record { int x; }>;

function errorBindingPattern8(error e) returns string {
    match e {
        var error MyError1 (a) if a is string && a == "Message" => {
            return "match1";
        }
        var error MyError1 (a, x = b) => {
            return "match2";
        }
    }
    return "No match";
}

function testErrorBindingPattern8() {
    error e1 = error MyError1("Message", x = 2);
    assertEquals("match1", errorBindingPattern8(e1));
    assertEquals("match2", errorBindingPattern8(error MyError1("Message1", x = 2)));
    assertEquals("match2", errorBindingPattern8(error MyError1("Message1", x = 4)));
    assertEquals("No match", errorBindingPattern8(error("Message1", x = "5")));
}

function testErrorBindingPattern9() {
    any|error err1 = error("Error", error("Invalid Op"));
    assertEquals("message:Error cause:Invalid Op", errorBindingPattern9(err1));
    assertEquals("message:Error cause:Invalid Op", errorBindingPattern10(err1));

    any|error err2 = error("Error");
    assertEquals("message:Error cause:No Cause", errorBindingPattern9(err2));
    assertEquals("message:Error cause:No Cause", errorBindingPattern10(err2));

    any|error err3 = error("Error", details = 12);
    assertEquals("message:Error cause:No Cause", errorBindingPattern9(err3));
    assertEquals("message:Error cause:No Cause", errorBindingPattern10(err3));
}

function errorBindingPattern9(any|error x) returns string {
    match x {
        var error(m, c) => {
            string m2 = m;
            error? c2 = c;
            return "message:"+ m2 + " cause:" + (c2 is () ? "No Cause" : c2.message());
        }
    }

    return "Default";
}

function errorBindingPattern10(any|error x) returns string {
    match x {
        error(var m, var c) => {
            string m2 = m;
            error? c2 = c;
            return "message:"+ m2 + " cause:" + (c2 is () ? "No Cause" : c2.message());
        }
    }

    return "Default";
}

function testErrorBindingPattern10() {
    any|error err1 = error("Error");
    assertEquals("message:Error cause:No Cause details:{}", errorBindingPattern11(err1));

    any|error err2 = error("Error", error("Invalid Op"));
    assertEquals("message:Error cause:Invalid Op details:{}", errorBindingPattern11(err2));

    any|error err3 = error("Error", error("Invalid Op"), code1 = 12);
    assertEquals("message:Error cause:Invalid Op details:{\"code1\":12}", errorBindingPattern11(err3));

    any|error err4 = error("Error", code1 = 12, code2 = {a: "A", b: [1, 2]});
    assertEquals("message:Error cause:No Cause details:{\"code1\":12,\"code2\":{\"a\":\"A\",\"b\":[1,2]}}",
    errorBindingPattern11(err4));

    any|error err5 = error("Error", code1 = {a: "A", b: [1, 2]});
    assertEquals("message:Error cause:No Cause details:{\"a\":\"A\",\"rest\":{\"b\":[1,2]}}",
    errorBindingPattern12(err5));

    any|error err6 = error("Error", code1 = [12, "A", 10.5, 1]);
    assertEquals("message:Error cause:No Cause details:{\"a\":12,\"rest\":[\"A\",10.5,1]}",
    errorBindingPattern13(err6));

    any|error err7 = error("Error", error("Invalid Op"), code1 = [12, "A", 10.5, 1], code2 = {a: "A", b: [1, 2]});
    assertEquals("message:Error cause:Invalid Op details:{\"a\":[12,\"A\",10.5,1],\"rest\":{\"code2\":{\"a\":\"A\"," +
    "\"b\":[1,2]}}}",
    errorBindingPattern14(err7));
}

function errorBindingPattern11(any|error x) returns string {
    match x {
        var error(m, c, code1 = d, code2 = e) => {
            string m2 = m;
            error? c2 = c;
            value:Cloneable code1 = d;
            value:Cloneable code2 = e;
            map<any|error> details = {};
            if !(code1 is ()) {
                details["code1"] = code1;
            }
            if !(code2 is ()) {
                details["code2"] = code2;
            }
            return "message:"+ m2 + " cause:" + (c2 is () ? "No Cause" : c2.message())
                   + " details:" + details.toString();
        }
    }

    return "Default";
}

function errorBindingPattern12(any|error x) returns string {
    match x {
        var error(m, c, code1 = {a: d, ...e}) => {
            string m2 = m;
            error? c2 = c;
            value:Cloneable x1 = d;
            value:Cloneable x2 = e;
            map<any|error> details = {};
            if !(x1 is ()) {
                details["a"] = x1;
            }
            if !(x2 is ()) {
                details["rest"] = x2;
            }
            return "message:"+ m2 + " cause:" + (c2 is () ? "No Cause" : c2.message())
                   + " details:" + details.toString();
        }
    }

    return "Default";
}

function errorBindingPattern13(any|error x) returns string {
    match x {
        var error(m, c, code1 = [d, ...e]) => {
            string m2 = m;
            error? c2 = c;
            value:Cloneable x1 = d;
            value:Cloneable x2 = e;
            map<any|error> details = {};
            if !(x1 is ()) {
                details["a"] = x1;
            }
            if !(x2 is ()) {
                details["rest"] = x2;
            }
            return "message:"+ m2 + " cause:" + (c2 is () ? "No Cause" : c2.message())
                   + " details:" + details.toString();
        }
    }

    return "Default";
}

function errorBindingPattern14(any|error x) returns string {
    match x {
        var error(m, c, code1 = d, ...e) => {
            string m2 = m;
            error? c2 = c;
            value:Cloneable x1 = d;
            value:Cloneable x2 = e;
            map<any|error> details = {};
            if !(x1 is ()) {
                details["a"] = x1;
            }
            if !(x2 is ()) {
                details["rest"] = x2;
            }
            return "message:"+ m2 + " cause:" + (c2 is () ? "No Cause" : c2.message())
                   + " details:" + details.toString();
        }
    }

    return "Default";
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
