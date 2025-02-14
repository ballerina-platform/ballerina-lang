// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function test1(int|string|float x) {
    if !(x is int) {
        string _ = x; // error incompatible types: expected 'string', found '(string|float)'
        float|string _ = x; // OK
    } else {
        int _ = x; // OK
    }
}

function test2(int|string|float x) {
    if !(x is int) {
        string _ = x; // error incompatible types: expected 'string', found '(string|float)'
        float|string _ = x; // OK
        return;
    }

    int _ = x; // OK
}

function test3(int|string|float|boolean x) {
    if !(x is int) {
        string _ = x; // error incompatible types: expected 'string', found '(string|float|boolean)'
        float|string|boolean _ = x; // OK

        if !(x is string) {
            string _ = x; // error incompatible types: expected 'string', found '(float|boolean)'
            float|boolean _ = x;

            if !(x is float) {
                boolean _ = x; // OK
                return;
            }
        }
    }

    int|string|float _ = x; // Type not narrowed. issue #34307
}

function test4(int|string|float x) {
    if !!!!(x !is int) {
        string _ = x; // error incompatible types: expected 'string', found '(string|float)'
        float|string _ = x; // OK
    } else {
        int _ = x; // OK
    }
}

function test5(1|2? x) {
    if !!(x == ()) {
        () _ = x; // OK
    } else if !!(+2 != x) {
        1 _ = x; // OK
    } else {
        2 _ = x; // OK
    }
}

function test6(int|string x) {
    if true && x is string {
        string _ = x;
    } else {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test7(int|string x) {
    if true && x is string {
        string _ = x;
        return;
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test8(int|string x) {
    if false && x is string {
        string _ = x; // error unreachable code
    } else {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test9(int|string x) {
    if false && x is string {
        string _ = x;
        return;
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test10(int|string x) {
    boolean b = true;
    if b && x is string {
        string _ = x;
    } else {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test11(int|string x) {
    boolean b = true;

    if x is string && b {
        string _ = x;
        return;
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test12(int|string|float x) {
    if x is string|int && x is string|float {
        string _ = x;
    } else {
        float|int _ = x; // OK
    }

    int|float _ = x; // error incompatible types: expected '(int|float)', found '(int|string|float)'
}

function test13(int|string|float x) {
    if x is string|int && x is string|float {
        string _ = x;
        return;
    }

    int _ = x; // error incompatible types: expected 'int', found '(float|int)'
    int|float _ = x; // OK
}

function test14_1(int|string|float x) {
    if !(x is string) && !(x is int) {
        float _ = x;
    } else {
        int _ = x; // error incompatible types: expected 'int', found '(string|int)'
        string|int _ = x; // OK
    }

    int|string _ = x; // error incompatible types: expected '(int|string)', found '(int|string|float)'
}

function test14_2(int|string|float x) {
    if x !is string && x !is int {
        float _ = x;
    } else {
        int _ = x; // error incompatible types: expected 'int', found '(string|int)'
        string|int _ = x; // OK
    }

    int|string _ = x; // error incompatible types: expected '(int|string)', found '(int|string|float)'
}

function test15_1(int|string|float x) {
    if !(x is string) && !(x is int) {
        float _ = x;
        return;
    }

    int|string _ = x; // OK
}

function test15_2(int|string|float x) {
    if x !is string && x !is int {
        float _ = x;
        return;
    }

    int|string _ = x; // OK
}

function test16_1(int|string|float x) {
    if !(x is string) && x is any {
        string _ = x; // error incompatible types: expected 'string', found '(int|float)'
        int|float _ = x; // OK
        return;
    }

    string _ = x; // OK
}

function test16_2(int|string|float x) {
    if x !is string && x is any {
        string _ = x; // error incompatible types: expected 'string', found '(int|float)'
        int|float _ = x; // OK
        return;
    }

    string _ = x; // OK
}

function test17(int|string x, boolean|float y, int|float z) {
    if x is int && y is float && z is int {
        int _ = x;
        float _ = y;
        int _ = z;
    } else {
        string _ = x; // error incompatible types: expected 'int', found '(string|int)'
        boolean _ = y; // error incompatible types: expected 'boolean', found '(boolean|float)'
        float _ = z; // error incompatible types: expected 'float', found '(int|float)'
    }
}

function test18(int|string x, boolean|float y, int|float z) {
    if x is int && y is float && z is int {
        int _ = x;
        float _ = y;
        int _ = z;
        return;
    }

    string _ = x; // error incompatible types: expected 'string', found '(string|int)'
    boolean _ = y; // error incompatible types: expected 'boolean', found '(boolean|float)'
    float _ = z; // error incompatible types: expected 'float', found '(int|float)'
}

function test19(int|string x) {
    if x is int && x == 2 {
        2 _ = x; // OK
    } else {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        string|int _ = x; // OK
    }
}

function test20(int|string x) {
    if x is int && x == 2 {
        2 _ = x; // OK
    } else {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        string|int _ = x; // OK
    }

    if !(x !is int) {
        int _ = x; // OK
    } else {
        string _ = x; // OK
    }

    if x is int && !(x !is int) {
        int _ = x; // Type not narrowed. issue #34965
    } else {
        string _ = x; // Type not narrowed. issue #34965
    }
}

function test21(int|string x) {
    if true || x is string {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        string|int _ = x; // OK
    } else {
        string _ = x; // error incompatible types: expected 'string', found 'int'
    }
}

function test22(int|string x) {
    if true || x is string {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        return;
    }

    string _ = x; // error incompatible types: expected 'string', found 'int'
}

function test23(int|string x) {
    if false || x is string {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
    } else {
        int _ = x; // OK
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test24(int|string x) {
    if false || x is string {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        string|int _ = x; // OK
        return;
    }

    int _ = x; // OK
}

function test25(int|string x) {
    boolean b = true;
    if x is string || b {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        string|int _ = x;
    } else {
        int _ = x; // OK
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test26(int|string x) {
    boolean b = true;

    if x is string || b {
        string _ = x; // error incompatible types: expected 'string', found '(string|int)'
        return;
    }

    int _ = x; // OK
}

function test27() {
    int|string|float x = 1;
    if x is int || x is float {
        int|float _ = x; // OK
        return;
    }

    string _ = x; // OK
}

function test28() {
    int|string|float x = 1;
    if x is int || x is float {
        int|float _ = x; // OK
        return;
    } else {
        string _ = x; // OK
    }

    string _ = x; // Type not narrowed. issue #34307
}

function test29_1(int|string|float x) {
    if x is int || !(x is string)  {
        int|float _ = x; // OK
    } else {
        string _ = x; // OK
    }

    string _ = x; // error incompatible types: expected 'string', found '(int|string|float)'
}

function test29_2(int|string|float x) {
    if x is int || x !is string  {
        int|float _ = x; // OK
    } else {
        string _ = x; // OK
    }

    string _ = x; // error incompatible types: expected 'string', found '(int|string|float)'
}

function test30_1(int|string|float x) {
    if x is int || !(x is string) {
        int|float _ = x; // OK
        return;
    }

    string _ = x; // OK
}

function test30_2(int|string|float x) {
    if x is int || x !is string {
        int|float _ = x; // OK
        return;
    }

    string _ = x; // OK
}

function test31(int|string x, boolean|float y) {
    if x is int || y is float {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
        int|string _ = x; // OK

        float _ = y; // error incompatible types: expected 'float', found '(float|boolean)'
        float|boolean _ = y; // OK
    } else {
        string _ = x; // OK
        boolean _ = y; // OK
    }
}

function test32(int|string x, boolean|float y) {
    if x is int || y is float {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
        int|string _ = x; // OK

        float _ = y; // error incompatible types: expected 'float', found '(float|boolean)'
        float|boolean _ = y; // OK

        return;
    }

    string _ = x; // OK
    boolean _ = y; // OK
}

function test33(2|"foo"|"bar"? x) {
    if x == "foo" || x == "bar" {
        "foo" _ = x; // error incompatible types
        "bar" _ = x; // error incompatible types
        "foo"|"bar" _ = x; // OK
    } else if x == +2 {
        (+2) _ = x;
        2 _ = x;
    } else {
        () _ = x; // OK
    }

    int _ = x; // error incompatible types: expected 'int', found '(bar|2|foo)?'
}
