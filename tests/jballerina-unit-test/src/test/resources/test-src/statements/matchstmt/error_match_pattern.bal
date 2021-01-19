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

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
