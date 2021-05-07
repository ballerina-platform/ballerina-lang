final int i = 100;
final int k = 200;

function closureVariableAsFieldValue() {
    final int i = 10;
    object {
        int x;
    } obj = object {
        int x = i;
    };

    assertValueEquality(10, obj.x);
}

function closureVariableAsFieldValueUsedInAttachedFunctions() {
    final int i = 10;
    object {
        int x;
        function bar(int b) returns int;

    } bOceVariable = object {
        int x = i;

        function bar(int b) returns int {
            return b + self.x;
        }
    };

    var lambda = bOceVariable.bar;
    int y = lambda(45);

    assertValueEquality(55, y);
    assertValueEquality(10, bOceVariable.x);
}

function closureVariableUsedInsideAttachedMethodBodyAndField() {
    final int i = 10;
    object {
        int x;
        function bar(int b) returns int;

    } bOceVariable = object {
        int x = i;

        function bar(int b) returns int {
            return b + i;
        }
    };

    var lambda = bOceVariable.bar;
    int y = lambda(45);

    assertValueEquality(10, bOceVariable.x);
    assertValueEquality(55, y);
}

function closureVariableUsedInsideAttachedMethodBodyOnly() {
    final int i = 10;
    object {
        int x;
        function bar(int b) returns int;

    } bOceVariable = object {
        int x = 3;

        function bar(int b) returns int {
            return b + i;
        }
    };

    var lambda = bOceVariable.bar;
    int y = lambda(45);

    assertValueEquality(3, bOceVariable.x);
    assertValueEquality(55, y);
}

function closureVariableUsedInsideWithDifferentType() {
    final string i = "10";
    object {
        int x;
        function bar(string b) returns string;
    } bOceVariable = object {
        int x = 3;

        function bar(string b) returns string {
            return b + i;
        }
    };

    var lambda = bOceVariable.bar;
    string y = lambda("45");

    assertValueEquality(3, bOceVariable.x);
    assertValueEquality("4510", y);
}

function testClosureButAsArgument() {
    closureVariableUsedInsideMethodNoBlockMap("10");
    closureArgumentAsClassFieldDefaultValue("10");
    closureArgumentAsClassFieldDefaultValueChangingClosureVarValue("10");
    closureVariablesAndArgsAsFieldsAndAttachedMethodBodyTogether(10);
}

function closureVariableUsedInsideMethodNoBlockMap(string j) {
    object {
        int x;
        function bar(string b) returns string;
    } bOceVariable = object {
        int x = 3;

        function bar(string b) returns string {
            return b + j;
        }
    };

    var lambda = bOceVariable.bar;
    string y = lambda("45");

    assertValueEquality(3, bOceVariable.x);
    assertValueEquality("4510", y);
}

function closureArgumentAsClassFieldDefaultValue(string j2) {
    object {
        string x;
        function bar(string b) returns string;
    } bOceVariable = object {
        string x = j2;

        function bar(string b) returns string {
            return b + self.x;
        }
    };

    var lambda = bOceVariable.bar;
    string y = lambda("45");

    assertValueEquality("4510", y);
}

function closureVariablesAndArgsAsFieldsAndAttachedMethodBodyTogether(int i) {
    final int k = 10;
    object {
        int x;
        int y;
        function bar(int b) returns int;
    } obj =
    object {
        int x = i;
        int y = k;

        function bar(int b) returns int {
            return b + i;
        }
    };

    var lambda = obj.bar;
    int y = lambda(45);
    assertValueEquality(55, y);
    assertValueEquality(10, obj.x);
    assertValueEquality(10, obj.y);
}

function closureLambda(string j) returns function (string b) returns string {
    object {
        function bar(string b) returns string;
    } bOceVariable = object {

        function bar(string b) returns string {
            return b + j;
        }
    };

    var lambda = bOceVariable.bar;
    return lambda;
}

function closureArgumentAsClassFieldDefaultValueChangingClosureVarValue(string j2) {
    string j = "10";
    object {
        function bar(string b) returns string;
    } bOceVariable = object {

        function bar(string b) returns string {
            return b + j;
        }
    };

    var lambda = bOceVariable.bar;
    string y = lambda("45");
    assertValueEquality("4510", y);

    j = "hi";
    y = lambda("45");
    assertValueEquality("45hi", y);

    lambda = closureLambda(j);
    y = lambda("45");
    assertValueEquality("45hi", y);

    j = "hello";
    y = lambda("45");
    assertValueEquality("45hi", y);
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
