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

function mappingBindingPatternRest1(any v) returns any|error {
    match v {
        var {w: a, x: b, y: c,  ...r} => {
            return r["z"];
        }
        var {x: a, y: b, ...r} => {
            return r["d"];
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPatternWithRest1() {
    assertEquals(3, <anydata> checkpanic mappingBindingPatternRest1({x: 2, y: 3, z: 3, w: 4}));
    assertEquals(1, <anydata> checkpanic mappingBindingPatternRest1({d: 1, x: 2, y: 3, z: "3"}));
    assertEquals("No match", <anydata> checkpanic mappingBindingPatternRest1({d: 3, y: 3, z: 3, w: 4}));
}

function mappingBindingPatternRest2(record { int x; int y; int z1; int z2; } v) returns anydata {
    match v {
        var {x: a, y: b, z1: c, ...r} => {
            return r["z2"];
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPatternWithRest2() {
    assertEquals(22, mappingBindingPatternRest2({x: 2, y: 3, z1: 5, z2: 22}));
}

function mappingBindingPatternRest3(map<int> v) returns anydata {
    match v {
        var {x: a, y: b, z1: c, ...r} => {
            return r["z2"];
        }
        var {x: a, y: b, ...r} => {
            return r["z2"];
        }
        var _ => {
            return "No match";
        }
    }
}

function testMappingBindingPatternWithRest3() {
    assertEquals(22, mappingBindingPatternRest3({x: 2, y: 3, z1: 5, z2: 22}));
    assertEquals(25, mappingBindingPatternRest3({x : 2, y: 3, z3: 6, z2: 25}));
    assertEquals("No match", mappingBindingPatternRest3({w: 2, y: 2, z1: 6, z2: 22}));
}

function mappingBindingPatternRest4(anydata v) returns anydata {
    match v {
        var {x: {y: a, ...r}} => {
            return r["z"];
        }
    }
    return "";
}

function testMappingBindingPatternWithRest4() {
    assertEquals("z", mappingBindingPatternRest4({x: {y: 1, z: "z" }}));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
