// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testConstMatchPattern1() {
    string[][] data = [
        [ "abc", "def", "ghi"],
        [ "jkl", "mno"],
        [ "pqr", "stu", "vwx", "yz"],
        ["123"]
    ];

    (string|int)[] result = [];
    from string[] row in data
        do {
            match row.length() {
                4 => {
                    result.push("4");
                }
                3 => {
                    result.push("3");
                }
                2 => {
                    result.push("2");
                }
                _ => {
                    result.push("1");
                }
            }
        };
    assertEquality(["3", "2", "4", "1"], result);

    result = [];
    from string[] row in data
        do {
            match row.length() {
                4 => {
                    result.push(row.length());
                }
                3 => {
                    result.push(row.length());
                }
                2 => {
                    result.push(row.length());
                }
                _ => {
                    result.push(row.length());
                }
            }
        };

    result = [];
    from var {id, name} in [{id: 1, name: "John"}, {id: 2, name: "Doe"}]
        do {
            match id {
                2 => {
                    result.push("2");
                    result.push(name);
                }
                _ => {
                    result.push("1");
                    result.push(name);
                }
            }
        };
    assertEquality(["1", "John", "2", "Doe"], result);

    result = [];
    from var {id, name} in [{id: 1, name: "John"}, {id: 2, name: "Doe"}, {id: 3, name: "Emma"}]
        do {
            match id {
                2 => {
                    result.push("2");
                    result.push(name);
                }
                _ if name == "Emma" => {
                    result.push(id);
                    result.push(name);
                }
            }
        };
    assertEquality(["2", "Doe", 3, "Emma"], result);
}

function testConstMatchPattern2() {
    (string|int)[] data = [1, 2, "a", "b", "c"];

    (string|int)[] result = [];
    from string|int x in data
        do {
            match x {
                1 => {
                    result.push(1);
                }
                2 => {
                    result.push(2);
                }
                "a" => {
                    result.push("a");
                }
                _ => {
                    result.push("x");
                }
            }
        };
    assertEquality([1, 2, "a", "x", "x"], result);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
