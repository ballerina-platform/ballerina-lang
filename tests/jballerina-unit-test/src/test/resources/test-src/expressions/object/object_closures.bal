final int i = 100;
final int k = 200;

function testClosureVariableAsFieldValue() {
    final int i = 10;
    object {
        int x;
    } obj = object {
        int x = i;
    };

    var inferredObj = object {
        int x = i;
    };

    var objectWithInit = object {
         int a = i;
         function init() {
             self.a = i;
         }
    };

    assertValueEquality(10, inferredObj.x);
    assertValueEquality(10, obj.x);
    assertValueEquality(10, objectWithInit.a);
}

function testClosureVariableAsFieldValueUsedInAttachedFunctions() {
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

    var inferredObj = object {
        int x = i;

        function bar(int b) returns int {
            return b + self.x;
        }
    };

    lambda = inferredObj.bar;
    y = lambda(45);

    assertValueEquality(55, y);
    assertValueEquality(10, inferredObj.x);
}

function testClosureVariableUsedInsideAttachedMethodBodyAndField() {
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

    var inferredObj = object {
        int x = i;

        function bar(int b) returns int {
            return b + i;
        }
    };

    lambda = inferredObj.bar;
    y = lambda(45);

    assertValueEquality(10, inferredObj.x);
    assertValueEquality(55, y);
}

function testClosureVariableUsedInsideAttachedMethodBodyOnly() {
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

    var inferredObj = object {
        int x = 3;

        function bar(int b) returns int {
            return b + i;
        }
    };

    lambda = inferredObj.bar;
    y = lambda(45);

    assertValueEquality(3, inferredObj.x);
    assertValueEquality(55, y);
}

function testClosureVariableUsedInsideWithDifferentType() {
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

    var inferredObj = object {
        int x = 3;

        function bar(string b) returns string {
            return b + i;
        }
    };

    lambda = inferredObj.bar;
    y = lambda("45");

    assertValueEquality(3, inferredObj.x);
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
    string j3 = j2;
    string j4 = j3;
    string j = j4;
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

int a = 200;

public function testAttachedMethodClosuresMapFromFunctionBlock() {
    int a = 10;
    var x = object {
        function t1() returns int {
            return 3 + a;
        }
    };
    int getValue = x.t1();
    assertValueEquality(x.t1(), 13);
}

type Sum2 function (int x, int y) returns int;

final Sum2 sumF = function (int x, int y) returns int {
    return x + y;
};

function testFunctionPointerAsFieldValue() {
    final int i = 10;
    object {
        function summer() returns int ;
    } obj = object {
        private Sum2 func = sumF;
        function summer() returns int {
            return self.func(4, i);
        }
    };

    assertValueEquality(14, obj.summer());
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
