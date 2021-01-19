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

function errorMatchPattern1(error e) returns anydata {
    match e {
        error("Message", x = 200, y = 300, ...var a) => {
            return a["z"];
        }
        error("Message", x = 200, ...var a) => {
            return a["z"];
        }
        error(_) => {
            return "No match";
        }
    }
}

function testErrorMatchPattern1() {
    assertEquals(400, errorMatchPattern1(error("Message", x = 200, y = 300, z = 400)));
    assertEquals(400, errorMatchPattern1(error("Message", x = 200, y = 301, z = 400)));
    assertEquals("No match", errorMatchPattern1(error("Message1")));
}

function errorMatchPattern2(error e) returns anydata {
    match e {
        error(x = 200, y = 300, ...var a) => {
            return a["z"];
        }
        error(x = 200, ...var a) => {
            return a["z"];
        }
        error(_) => {
            return "No match";
        }
    }
}

function testErrorMatchPattern2() {
    assertEquals(400, errorMatchPattern2(error("Message", x = 200, y = 300, z = 400)));
    assertEquals(400, errorMatchPattern2(error("Message", x = 200, y = 301, z = 400)));
    assertEquals("No match", errorMatchPattern2(error("Message1")));
}

function errorMatchPattern3(error e) returns anydata {
    match e {
        error("Message1", error("Message2", c = 200, ...var a)) => {
            return a["d"];
        }
    }
    return "No match";
}

function testErrorMatchPattern3() {
    assertEquals(300, errorMatchPattern3(error("Message1", error("Message2", c = 200, d = 300, e = 400))));
    assertEquals("No match", errorMatchPattern3(error("Message1")));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
