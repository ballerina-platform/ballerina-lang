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

const C = "C";
function testBindingPatternsInMatchStatement() {
    (anydata|error)[] expected = ["A", "C", 1, 100, [1], [2, 3], [4, 5, 6, 7], [8, [9, 10]],
                         {a: 3, b: 20}, {t: {a: 3, b: 20}}, {a: 3, b: 20, c: 40, d: 500},
                         error("Generic Error", code = 20)];
    (anydata|error)[] result = [];
    from anydata|error item in expected
        do  {
            int z = 100;
            match item {
                "A" => {
                    result.push("A");
                }
                
                C => {
                    result.push("C");
                }
                
                100 => {
                    result.push(z);
                }

                var i if i is int => {
                    result.push(i);
                }

                var [_] => {
                    result.push(item);
                }
    
                var [i, j] if j is int => {
                    result.push([i, j]);
                }

                var [i, [j, k]] => {
                    result.push([i, [j, k]]);
                }

                var [i, j, ...rest] => {
                    result.push([i, j, ...rest]);
                }
                
                var {a, b, ...rest} if rest.cloneReadOnly().length() > 0 => {
                    result.push({a, b, ...rest});
                }

                var {a, b} => {
                    result.push({a, b});
                }

                var {t: {a, b}} => {
                    result.push({t: {a, b}});
                }
                
                var error(ERROR, code = code) => {
                    result.push(error(ERROR, code = code));
                }
                
                _ if item is anydata => {
                    result.push(item);
                }
            }
        };
    foreach int i in 0...result.length() - 1 {
        assertEquality(expected[i], result[i]);
    } 
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata|error expected, anydata|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    
    if expected is error && actual is error && expected.message() == actual.message(){
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
