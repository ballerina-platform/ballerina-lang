// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// ========================== Basics ==========================

function testValueTypeInUnion() returns string {
    int|string x = 5;
    if (!(x is int)) {
        int y = x;
        return "int: " + y.toString();
    } else {
        string s = x;
        return s;
    }
}

type A record {|
    string a;
|};

type B record {|
    string b;
    string c;
|};

function testSimpleRecordTypes_1() returns string {
    A x = {a:"foo"};
    any y = x;
     if (y is A) {
        return y["b"] + "-" + y["c"];
    } else if (y is B) {
        return y["a"];
    }

    return "n/a";
}


function testSimpleTernary() returns string {
    any a = "hello";
    return a is string ? (a is int ? "a is a string and an int" : "a is a string and not an int") : "a is not a string";
}

function testTypeGuardsWithAnd() returns string {
    int|string x = 5;
    if (x is int && x > 4) {
        int y = x;
        return "x is greater than 4: " + y.toString();
    } else {
        string s = x;
        return s;
    }
}

function testUndefinedSymbol() returns string {
    if a is string {
        return a;
    }

    return "";
}

function testTypeGuardInElse_1() returns string {
    int|string x = 5;
    if (x is int) {
        int y = x;
        return "int: " + y.toString();
    } else {
        if (x is string) {
            return "string: " + x;
        }

        if (x is int) {
            return "int: " + x.toString();
        }
    }

    return "n/a";
}

function testTypeGuardInElse_2() returns string {
    int|string|float|boolean x = true;
    int|string|float|boolean y = false;
    if (x is int|string) {
        if (y is string) {
            return "y is string: " + y;
        } else if (y is int) {
            int i = y;
            return "y is int: " + i.toString();
        } else {
            return "x is int|string";
        }
    } else if (x is string) {
        return "string: " + x;
    } else if (x is float) {
        float f = x;
        return "float: " + f.toString();
    } else {
        if (y is int) {
            int i = y;
            return "x is boolean and y is int: " + i.toString();
        } else if (y is string) {
            return "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            return "x is boolean and y is float: " + f.toString();
        } else {
            boolean b = y;
            return "x is boolean and y is boolean: " + b.toString();
        }
    }
}

function testMultipleTypeGuardsWithAndOperator_2() returns int {
    int|string|boolean x = 5;
    any y = 7;
    if (x is int|string && y is int && x is string) {
        return y.sum(x);
    } else {
        x = 5.5;
        return -1;
    }

    x = {};
    return -1;
}

function typeGuardInMatch([string, int]|[int, boolean]|int|float x) returns string {
    match x {
        var [s, i] if s is string => {return "Matched with string";}
        var [s, i] if s is float => {return "Matched with float";}
        var [s, i] if i is boolean => {return "Matched with boolean";}
        var y => {return "Matched with default type - float";}
    }
}

function testTypeGuardsWithOr_1() returns string {
    int|string x = 5;
    int|string y = 8;
    if (x is int || y is int) {
        int z = x;
        return "x is greater than 4: " + z.toString();
    } else {
        string s = x;
        return s;
    }
}

function testTypeGuardsWithOr_2() returns string {
    int|string|boolean x = 5;
    int y = 8;
    if (x is int|string) {
        if (x is int || y > 4) {
            int z = x;
            return "x is greater than 4: " + z.toString();
        } else {
            string s = x;
            return s;
        }
    } else {
        return "x is boolean: " + x.toString();
    }
}

function testTypeGuardsWithBinaryOps_1() {
    int|string|boolean|float x = 5;
    if (((x is int|string && x is int) || (x is boolean)) && (x is float)) {
        int y = x;
    } else {
        string s = x;
    }
}

function testTypeGuardsWithBinaryOps_2() {
    int|string|boolean|float x = 5;
    if (((x is int|string || x is int) && (x is boolean)) || (x is float)) {
        int y = x;
    } else {
        string s = x;
    }
}

function testTypeGuardsWithBinaryOps_3() {
    int|string|boolean|float x = 5;
    if ((x is int|string && x is int) || (x is boolean)) {
        int y = x;
    } else {
        string s = x;
    }
}

function testTypeGuardsWithBinaryOps_4() {
    int|string|boolean|float x = 5;
    if ((x is string && x is int && x is float) || (x is boolean)) {
        int y = x;
    } else {
        string s = x;
    }
}

function testTypeGuardsWithBinaryOps_5() {
    int|string|boolean|float x = 5;
    if ((x is string && x is T && x is float) || (x is boolean)) {
        int y = x;
    } else {
        string s = x;
    }
}

type Person record {
    string name;
    int age;
};

type Student record {
    *Person;
    float gpa;
};

function testTypeGuardsWithBinaryOps_6() {
    Student s = {name:"John", age:20, gpa:3.5};
    Person|Student x = s;

    if ((x is string && x is Person && x is float) || (x is boolean)) {
        int y = x;
    } else {
        string y = x;
    }
}

function testTypeGuardsWithErrorInmatch() returns string {
    any a = 5;
    match a {
        var p if p is error => {return string `${p.reason()}`;}
        var p => {return "Internal server error";}
    }
}

public function testUpdatingTypeNarrowedVar_1() {
    int|string|boolean x = 5;

    if (x is int|string) {
        x = true;
        int y = x;

        if (x is int) {
            int z = x;
        } else {
            string z = x;
        }
    }
}

public function testUpdatingTypeNarrowedVar_2() returns string {
    int|string|boolean x = 8;
    if (x is int) {
        if (x > 5) {
            x = "hello";
        }

        int z = x;
        return "int:  + ";
    }

    return "not an int";
}

public function testUpdatingTypeNarrowedVar_3() {
    int|string|boolean x = 8;
    int|string|boolean y = 8;

    if (x is int|string) {
        int|string a = x;
        if (x is int) {
            int b = x;
            if (x > 5) {

                int c = x;
                x = "hello";
                int d = x;

                if (y is int) {
                    int e = x;
                    y = "hello again";
                    int f = x;
                }
                int g = x;
                string h = y;
            }
            int i = x;
            string j = y;
        }

        int|string j = x;
        string h = y;
    }

    int|string|boolean i = x;
}

string|int si = "hello world";

function testGlobalVarInTypeGuard() {
    if (si is string) {
        string s2 = si;
    } else {
        int i = si;
    }
}

function testTupleDestructuringAssignmentTypeResetting() {
    [string?, int] [s, i] = tupleReturningFunc("str");
    if (s is string) {
        [s, i] = tupleReturningFunc(());
        string strVal = s;
    }
}

function tupleReturningFunc(string? s) returns [string?, int] {
    return [s, 1];
}

function testRecordDestructuringAssignmentTypeResetting() {
    record {
        string s;
        int? i;
    } {s, i, ...rest} = recordReturningFunc(1);
    if (i is int) {
        {s: s, i: i, ...rest} = recordReturningFunc(2);
        int intVal = i;
    }
}

function recordReturningFunc(int? i) returns record {string s; int? i;} {
    return {s: "hello", i: i, "f": 1.0};
}

function testErrorDestructuringAssignmentTypeResetting() {
    var error(s, message = message, code = code) = errorReturningFunc(1);
    if (code is int) {
        error(s, message = message, code = code) = errorReturningFunc(());
        int intVal = code;
    }
}

type Detail record {
    string message?;
    error cause?;
    int? code;
};

function errorReturningFunc(int? i) returns error<string, Detail> {
    return error("hello", message = "hello", code = i, f = 1.0);
}

function testInvalidAccessOfOutOfScopeVar() {
    int? i = ();

    if i is int {
        int j = 100;
        i = ();
    } else {
        () n = i;
        int k = j; // undefined symbol 'j'
    }
}
