// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
function testSimpleTernary() returns string {
    any a = "hello";
    return a is string ? (a is int ? "a is a string and an int" : "a is a string and not an int") : "a is not a string";
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
        return 1;
    } else {
        x = 1;
        return -1;
    }

    x = 6;
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

function testTypeGuardsWithBinaryOps_1() {
    int|string|boolean|float x = 5;
    if (((x is int|string && x is int) || (x is boolean)) && (x is float)) {
        int y = 1;
    } else {
        string s = "";
    }
}

function testTypeGuardsWithBinaryOps_2() {
    int|string|boolean|float x = 5;
    if (((x is int|string || x is int) && (x is boolean)) || (x is float)) {
        int y = 1;
    } else {
        string s = "";
    }
}

function testTypeGuardsWithBinaryOps_4() {
    int|string|boolean|float x = 5;
    if ((x is string && x is int && x is float) || (x is boolean)) {
        int y = 1;
    } else {
        string s = "";
    }
}

function testTypeGuardsWithBinaryOps_5() {
    int|string|boolean|float x = 5;
    if ((x is string && x is float) || (x is boolean)) {
        int y = 1;
    } else {
        string s = "";
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
        int y = 1;
    } else {
        string y = "";
    }
}

function testTypeGuardsWithErrorInmatch() returns string {
    any a = 5;
    match a {
        var p if p is error => {return string `${p.reason()}`;}
        var p => {return "Internal server error";}
    }
}
