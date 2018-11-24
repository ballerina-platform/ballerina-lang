// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Rec1 record {
    int | float a;
};

function recordTypes() returns string {
    Rec1 r1 = {a: 200};
    match r1 {
        {a: 200, b: "A"} => return "A";
        {a: 200, b: "A"} => return "A"; // unreachable pattern
        {b: "A", a: 200} => return "A"; // unreachable pattern
        {a: 100, b: "A"} => return "A";
        {a: 200, b: "B"} => return "A";
        {a: 150, b: "A", c: true} => return "A";
        {a: 150, b: "A"} => return "A";
        {a: 150, b: "A", c: false} => return "A"; // unreachable pattern
    }

    return "Fail";
}

function tupleTypes() returns string {
    (string, int) t1 = ("A", 12);
    match t1 {
        ("A", 15) => return "T";
        ("A", 15) => return "T"; // unreachable pattern
        ("B", 15) => return "T";
        ("B", 15) => return "T"; // unreachable pattern
    }

    return "Fail";
}

function simpleTypes() returns string {
    anydata k = 10;
    match k {
        15 => return "T";
        (12, 15) => return "T";
        20 => return "T";
        20 => return "T"; // unreachable pattern
        15 => return "T"; // unreachable pattern
        (12, 15) => return "T"; // unreachable pattern
    }

    return "Fail";
}
