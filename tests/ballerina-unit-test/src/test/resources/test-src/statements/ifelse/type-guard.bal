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

function testValueTypeInUnion() returns string {
    int|string x = 5;
    if (x is int) {
        int y = x;
        return "int: " + <string> y;
    } else {
        return "string";
    }
}

type A record {
    string a;
};

type B record {
    string b;
    string c;
};

function testSimpleRecordTypes_1() returns string {
    A x = {a:"foo"};
    any y = x;
     if (y is A) {
        return y.a;
    } else if (y is B) {
        return y.b + "-" + y.c;
    }

    return "n/a";
}

function testSimpleRecordTypes_2() returns string {
    B x = {b:"foo", c:"bar"};
    any y = x;
     if (y is A) {
        return y.a;
    } else if (y is B) {
        return y.b + "-" + y.c;
    }

    return "n/a";
}

function testSimpleTernary() returns string {
    any a = "hello";
    return a is string ? a : "not a string";
}

function testMultipleTypeGuardsWithAndOperator() returns int {
    int|string x = 5;
    any y = 7;
    if (x is int && y is int) {
        return x + y;
    } else {
        return -1;
    }
}

function testMultipleTypeGuardsWithAndOperatorInTernary() returns int {
    int|string x = 5;
    any y = 7;
    return (x is int && y is int) ? x + y  : -1;
}

function testTypeGuardInElse_1() returns string {
    int|string x = 5;
    if (x is int) {
        int y = x;
        return "int: " + <string> y;
    } else {
        return "string";
    }
}

function testTypeGuardInElse_2() returns string {
    int|string|float|boolean x = true;
    if (x is int) {
        int y = x;
        return "int: " + <string> y;
    } else if (x is string) {
        return "string: " + x;
    } else if (x is float) {
        float y = x;
        return "float: " + <string> y;
    } else {
        boolean b = x;
        return "boolean: " + <string> b;
    }
}

function testTypeGuardInElse_3() returns string {
    int|string|float|boolean x = true;
    int|string|float|boolean y = false;
    if (x is int|string) {
        if (y is string) {
            return "y is string: " + y;
        } else if (y is int) {
            int i = y;
            return "y is float: " + <string> i;
        } else {
            return "x is int|string";
        }
    } else if (x is string) {
        return "string: " + x;
    } else if (x is float) {
        float f = x;
        return "float: " + <string> f;
    } else {
        if (y is int) {
            int i = y;
            return "x is boolean and y is int: " + <string> i;
        } else if (y is string) {
            return "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            return "x is boolean and y is float: " + <string> f;
        } else {
            boolean b = y;
            return "x is boolean and y is boolean: " + <string> b;
        }
    }
}

function testTypeGuardInElse_4() returns string {
    int|string|float|boolean x = true;
    int|string|float|boolean y = false;
    string val = "1st round: ";
    if (x is int|string) {
        if (y is string) {
            val += "y is string: " + y;
        } else if (y is int) {
            int i = y;
            val += "y is float: " + <string> i;
        } else {
            val += "x is int|string";
        }
    } else if (x is string) {
        val += "string: " + x;
    } else if (x is float) {
        float f = x;
        val += "float: " + <string> f;
    } else {
        if (y is int) {
            int i = y;
            val += "x is boolean and y is int: " + <string> i;
        } else if (y is string) {
            val += "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            val += "x is boolean and y is float: " + <string> f;
        } else {
            boolean b = y;
            val += "x is boolean and y is boolean: " + <string> b;
        }
    }

    val += " | 2nd round: ";
    if (x is int|string) {
        if (y is string) {
            val += "y is string: " + y;
        } else if (y is int) {
            int i = y;
            val += "y is float: " + <string> i;
        } else {
            val += "x is int|string";
        }
    } else if (x is string) {
        val += "string: " + x;
    } else if (x is float) {
        float f = x;
        val += "float: " + <string> f;
    } else {
        if (y is int) {
            int i = y;
            val += "x is boolean and y is int: " + <string> i;
        } else if (y is string) {
            val += "x is boolean and y is string: " + y;
        } else if (y is float) {
            float f = y;
            val += "x is boolean and y is float: " + <string> f;
        } else {
            boolean b = y;
            val += "x is boolean and y is boolean: " + <string> b;
        }
    }

    return val;
}

function testTypeGuardInElse_5() returns string {
    int|string|float|boolean x = 5;
    if (x is int|string) {
        if (x is string) {
            return "x is string: " + x;
        } else if (x is int) {
            int i = x;
            return "x is int: " + <string> i;
        } else {
            return "x is int|string";
        }
    } else if (x is string) {
        return "string: " + x;
    } else if (x is float) {
        float f = x;
        return "float: " + <string> f;
    } else {
        return "x is boolean: " + <string> x;
    }
}

function testTypeGuardInElse_6() returns string {
    int|string|table<record {}> x = 5;
    if (x is table<record {}>) {
        table<record {}> t = x;
        return "table";
    } else {
        int|string y = x;
        if (y is string) {
            string s = y;
            return "string: " + y;
        } else {
            int i = y;
            return "int: " + i;
        }
    }
}


function testTypeGuardInElse_7() returns string {
    int|string|table<A> x = 5;
    if (x is table<A>) {
        table<A> t = x;
        return "table";
    } else {
        int|string y = x;
        if (y is string) {
            string s = y;
            return "string: " + y;
        } else {
            int i = y;
            return "int: " + i;
        }
    }
}

function testComplexTernary_1() returns string {
    int|string|float|boolean|int[] x = "string";
    return x is int ? "int" : (x is float ? "float" : (x is boolean ? "boolean" : (x is int[] ? "int[]" : x)));
}

function testComplexTernary_2() returns string {
    int|string|float|boolean|xml x = "string";
    if (x is int|string|float|boolean) {
        return x is int ? "int" : (x is float ? "float" : (x is boolean ? "boolean" : x));
    } else {
        xml y = x;
        return "xml";
    }
}

function testArray() returns int {
    int [] intArr = [10, 20];
    any[] arr = intArr;
    if (arr is int[]) {
        return arr[1];
    } else {
        return -1;
    }
}

function testUpdatingGuardedVar_1() returns string {
    any value = "BALLERINA";
    if (value is int|string|float) {
        if (value is string) {
         value = value + " - updated";
        } else {
            return "an int or float";
        }
    } else {
        return "some other type";
    }

    return string.convert(value);
}

function testUpdatingGuardedVar_2() returns string {
    any value = "BALLERINA";
    if (!(value is int|string|float)) {
        return "some other type";
    } else {
        if (value is string) {
            value = value + " - updated once";
            value = getUpdatedString(value);
        } else {
            return "an int or float";
        }
    }

    return string.convert(value);
}

function getUpdatedString(string s) returns string {
    return s + " - updated via function";
}

type func function() returns boolean;
int fPtrFlag = 0;

function testFuncPtrTypeInferenceInElseGuard() returns (boolean, int) {
    func? f = function () returns boolean {
        fPtrFlag = 100;
        return true;
    };

    if (f is ()) {
        return (false, fPtrFlag);
    } else {
        return (f.call(), fPtrFlag);
    }
}
