// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

int[] a = [];

int[][] b = [[1, 2], a]; // a is not an isolated expression here

int[][] c = [[1, 2], a.clone()];

int[] d;

function init() {
    lock {
        d = []; // isolated expression but isolated variables can't be module-no-init-var-decls
    }
}

function f1() {
    lock {
        // `b` isn't an isolated variable because it is not initialized with an isolated expression
        _ = b;
    }
}

function f2() {
    lock {
        // `c` is an isolated variable
        _ = c;
    }
}

function f3() {
    lock {
        // `d` isn't an isolated variable because it is a no-init-var-decl
        _ = d;
    }
}

function f4() returns int[][] {
    lock {
        return c.clone();
    }
}

function f5(boolean bool) {
    lock {
        int[][] _ = bool ? f4() : c;
    }
}

function f6(boolean bool) {
    lock {
        int[][] _ = bool ? f4() : b;
    }
}

configurable boolean conf = true;

int[][] e = conf ? [] : f4();

function f7() {
    lock {
        _ = e;
    }
}

int[][] f = [];

function f8() returns int[][] {
    return f;
}

int[][] g = conf ? [] : f8(); // not initialized with isolated expression in then case

function f9() {
    lock {
        _ = g;
    }
}

public function testIsolatedInference() {
    assertTrue(f1 is function ());
    assertFalse(f1 is isolated function ());
    assertTrue(f2 is isolated function ());
    assertFalse(f3 is isolated function ());
    assertTrue(f4 is isolated function () returns int[][]);
    assertTrue(f5 is isolated function (boolean));
    assertFalse(f6 is isolated function (boolean));
    assertTrue(f7 is isolated function ());
    assertFalse(f8 is isolated function () returns int[][]);
    assertFalse(f9 is isolated function ());
}

isolated function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

isolated function assertFalse(anydata actual) {
    assertEquality(false, actual);
}

isolated function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}', found '${actual.toBalString()}'`);
}
