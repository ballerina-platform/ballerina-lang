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

function errorMatchPattern1(any|error e) returns string {
    match e {
        error("Message") => {
            return "match1";
        }
        error(MSG) => {
            return "match2";
        }
        error(_) => {
            return "match3";
        }
    }
    return "No match";
}

function testErrorMatchPattern1() {
    assertEquals("match1", errorMatchPattern1(error("Message")));
    assertEquals("match2", errorMatchPattern1(error(MSG)));
    assertEquals("match2", errorMatchPattern1(error("Const Message")));
    assertEquals("match3", errorMatchPattern1(error("Const Message1")));
    assertEquals("No match", errorMatchPattern1("Message1"));
}

function errorMatchPattern2(error e) returns string {
    match e {
        error(var a) => {
            return "match1";
        }
    }
    return "No match";
}

function testErrorMatchPattern2() {
    assertEquals("match1", errorMatchPattern2(error("Message")));
}

function errorMatchPattern3(error e) returns string {
    match e {
        error(var a) if a is string => {
            return <string> a;
        }
    }
    return "No match";
}

function testErrorMatchPattern3() {
    assertEquals("Message", errorMatchPattern3(error("Message")));
}

function errorMatchPattern4(error e) returns string {
    match e {
        error("Message1", error("Message2")) => {
            return "match1";
        }
        error(MSG, error(MSG)) => {
            return "match2";
        }
        error(var a, error("Message")) => {
            return "match3";
        }
    }
    return "No match";
}

function testErrorMatchPattern4() {
    assertEquals("match1", errorMatchPattern4(error("Message1", error("Message2"))));
    assertEquals("match2", errorMatchPattern4(error(MSG, error(MSG))));
    assertEquals("match3", errorMatchPattern4(error("Message2", error("Message"))));
}

function errorMatchPattern5(error e) returns string {
    match e {
        error("Message1", var a) => {
            return "match1";
        }
    }
    return "No match";
}

function testErrorMatchPattern5() {
    assertEquals("match1", errorMatchPattern5(error("Message1", error("Message1"))));
    assertEquals("No match", errorMatchPattern5(error("Message2")));
}

function errorMatchPattern6(error e) returns string {
    match e {
        error("Message1", var a) if a is error && (<error> a).message() == "Old Message" => {
            return "match";
        }
        error("Message1", var a) => {
            return (<error> a).message();
        }
    }
    return "No match";
}

function testErrorMatchPattern6() {
    assertEquals("Message2", errorMatchPattern6(error("Message1", error("Message2"))));
    assertEquals("match", errorMatchPattern6(error("Message1", error("Old Message"))));
}

function errorMatchPattern7(error e) returns string {
    match e {
        error("Message1", error("Message2"), c1=200) => {
            return "match1";
        }
        error("Message1", error("Message2"), c1=2) | error("Message1", error("Message2"), c1=4) => {
            return "match2";
        }

    }
    return "No match";
}

function testErrorMatchPattern7() {
    assertEquals("match1", errorMatchPattern7(error("Message1", error("Message2"), c1=200)));
    assertEquals("match2", errorMatchPattern7(error("Message1", error("Message2"), c1=2)));
    assertEquals("match2", errorMatchPattern7(error("Message1", error("Message2"), c1=4)));
    assertEquals("No match", errorMatchPattern7(error("Message1", error("Old Message"))));
}

type MyError1 error<record { int x; }>;

function errorMatchPattern8(error e) returns string {
    match e {
        error MyError1 ("Message") => {
            return "match1";
        }
        error MyError1 ("Message1", x = 2) => {
            return "match2";
        }
        error MyError1 (x = 4) => {
            return "match3";
        }
        error MyError1 (a = 2) => {
            return "match4";
        }
    }

    return "No match";
}

function testErrorMatchPattern8() {
    assertEquals("match1", errorMatchPattern8(error MyError1("Message", x = 2)));
    assertEquals("match2", errorMatchPattern8(error MyError1("Message1", x = 2)));
    assertEquals("match3", errorMatchPattern8(error MyError1("Message1", x = 4)));
    assertEquals("No match", errorMatchPattern8(error MyError1("Message1", x = 1)));
}

function errorMatchPattern9(error e) returns string {
    match e {
        error MyError1() => {
            return "match1";
        }
        error () => {
            return "match2";
        }
    }

    return "No match";
}

function testErrorMatchPattern9() {
    assertEquals("match1", errorMatchPattern9(error MyError1("Message", x = 2)));
    assertEquals("match2", errorMatchPattern9(error("Message1")));
}

function testErrorMatchPattern10() {
    any|error e = error("Message1", error("Message3"));
    string result = "Not Matched";
    match e {
        error("Message1", error("Message2")) => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }

    }
    assertEquals("Not Matched", result);
}

function testErrorMatchPattern11() {
    any|error e = error("Message1", det1 = "detail1", det2 = "detail2");
    string result = "Not Matched";
    match e {
        error("Message1", det1 = var det1, det2 = var det2) => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Matched", result);
}

function testErrorMatchPattern12() {
    any|error e = error("Message1", det1 = "detail1", det3 = "detail3");
    string result = "Not Matched";
    match e {
        error("Message1", det1 = var det1, det2 = var det2) => {
            result = "Matched";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Not Matched", result);
}

function testErrorMatchPattern13() {
    any|error e = error("Message1", det1 = "detail1", det3 = "detail3");
    string result = "Not Matched";
    match e {
        error("Message1", det1 = var det1, ...var rest) => {
            if (rest["det3"] == "detail3") {
                result = "Matched";
            }
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Matched", result);
}

function testErrorMatchPattern14() {
    any|error e = error("Message1");
    string result = "Not Matched";
    match e {
        error("Message1", det1 = var det1) => {
            result = "Matched with incorrect error";
        }
        error("Message1") => {
            result = "Matched";
        }
    }
    assertEquals("Matched", result);
}

function testErrorMatchPattern15() {
    error err1 = error("Error One", data = [11, 12]);
    assertEquals("Matched with Error One", errorMatchPattern15(err1));

    error err2 = error("Error Two", data = [11, {a: "A", b: "B"}]);
    assertEquals("Matched with Error Two", errorMatchPattern15(err2));
}

function errorMatchPattern15(error x) returns string {
    match x {
        error("Error One", data=[11, 12]) => {
            return "Matched with Error One";
        }
        error("Error One", data={a: "A", b: "B"}) => {
            return "Some Other Error";
        }
        error("Error Two", data=[11, {a: "A", b: "B"}]) => {
            return "Matched with Error Two";
        }
    }

    return "Default";
}

function testErrorMatchPattern16() {
    any|error err1 = error("Error");
    assertEquals("message:Error cause:No Cause details:{}", errorMatchPattern16(err1));

    any|error err2 = error("Error", error("Invalid Op"));
    assertEquals("message:Error cause:Invalid Op details:{}", errorMatchPattern16(err2));

    any|error err3 = error("Error", error("Invalid Op"), code1 = 12);
    assertEquals("message:Error cause:Invalid Op details:{\"code1\":12}", errorMatchPattern16(err3));

    any|error err4 = error("Error", code1 = 12, code2 = {a: "A", b: [1, 2]});
    assertEquals("message:Error cause:No Cause details:{\"code1\":12,\"code2\":{\"a\":\"A\",\"b\":[1,2]}}",
    errorMatchPattern16(err4));

    any|error err5 = error("Error", code1 = {a: "A", b: [1, 2]});
    assertEquals("message:Error cause:No Cause details:{\"a\":\"A\",\"rest\":{\"b\":[1,2]}}",
    errorMatchPattern17(err5));

    any|error err6 = error("Error", code1 = [12, "A", 10.5, 1]);
    assertEquals("message:Error cause:No Cause details:{\"a\":12,\"rest\":[\"A\",10.5,1]}",
    errorMatchPattern18(err6));

    any|error err7 = error("Error", error("Invalid Op"), code1 = [12, "A", 10.5, 1], code2 = {a: "A", b: [1, 2]});
    assertEquals("message:Error cause:Invalid Op details:{\"a\":[12,\"A\",10.5,1],\"rest\":{\"code2\":{\"a\":\"A\"," +
    "\"b\":[1,2]}}}",
    errorMatchPattern19(err7));
}

function errorMatchPattern16(any|error x) returns string {
    match x {
        error(var m, var c, code1 = var d, code2 = var e) => {
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

function errorMatchPattern17(any|error x) returns string {
    match x {
        error(var m, var c, code1 = {a:var d, ...var e}) => {
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

function errorMatchPattern18(any|error x) returns string {
    match x {
        error(var m, var c, code1 = [var d, ...var e]) => {
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

function errorMatchPattern19(any|error x) returns string {
    match x {
        error(var m, var c, code1 = var d, ...var e) => {
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
