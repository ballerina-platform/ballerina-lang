// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;

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

function testClosureVariableAsFieldValueWithExpression() {
    final int i = 10;
    object {
        int x;
        function bar(int b) returns int;

    } bOceVariable = object {
        int x = i + 4;

        function bar(int b) returns int {
            return b + self.x;
        }
    };

    var lambda = bOceVariable.bar;
    int y = lambda(45);

    assertValueEquality(59, y);
    assertValueEquality(14, bOceVariable.x);
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
    assertValueEquality(getValue, 13);
}

type Sum2 function (int x, int y) returns int;

final Sum2 sumF = function(int x, int y) returns int {
    return x + y;
};

function testFunctionPointerAsFieldHelper(int y2) {
    final int i = 10;
    object {
        function summer(int y2) returns int;
    } obj = object {
        int x;
        private Sum2 func = sumF;
        function init() {
            self.x = y2;
        }
        function summer(int y) returns int {
            return self.func(4, i + y + self.x);
        }
    };

    assertValueEquality(4 + 10 + 5 + 3, obj.summer(5));
}

function testFunctionPointerAsFieldValue() {
    testFunctionPointerAsFieldHelper(3);
}

function checkClosuresWithObjectConstrExpr(string a) returns string {
    string b = "A";
    object {
        string x;
        function foo(string c) returns string;
    } obj1 = object {
        string x = a;
        function foo(string c) returns string {
            return a + b + self.x + c;
        }
    };

    return obj1.foo("B") + obj1.x;
}

final string A = "A";
const B = "B";

function checkClosuresWithObjectConstrExprAsFunctionDefaultParam(object {
                                                                     string x;
                                                                     function foo(string c) returns string;
                                                                 } obj = object {
                                                                     string x = A;
                                                                     function foo(string c) returns string {
                                                                         return A + B + self.x + c;
                                                                     }
                                                                 }) returns string {
    return A + B + obj.x + obj.foo("C");
}

int intVal = 60;

function checkClosuresWithObjectConstrExprInAnonFunc() returns (function () returns int) {
    return function() returns int {
        int a1 = 20;
        var obj1 = object {
            int a2 = 30;
            function foo(int a3) returns int {
                return a1 + self.a2 + a3 + intVal;
            }
        };
        return obj1.foo(50);
    };
}

function checkClosuresWithObjectConstrExprInObjectFunc(int b1) returns int|error? {
    int a1 = 10;
    var obj1 = object {
        int a2 = 20;
        function foo(int b2) returns int|error? {
            var obj2 = object {
                int a3 = 30;

                function bar(string b3) returns int|error? {
                    return a1 + self.a3 + check int:fromString(b3);
                }

                function bam(int|error? b4) returns int {
                    if b4 is int {
                        return b4;
                    }
                    return intVal;
                }
            };
            return obj2.bam(obj2.bar("123")) + self.a2;
        }
    };
    return obj1.foo(50);
}

function checkClosuresWithObjectConstrExprInVarAssignment(int b1) returns int {
    final int a1 = 10;
    object {
        int a2;
        function foo(int b2) returns int;
    } obj1;

    obj1 = object {
        int a2 = b1;
        function foo(int b2) returns int {
            var obj2 = object {
                int a3 = a1;

                function bar(int b3) returns int {
                    return a1 + self.a3 + b3 + intVal;
                }

            };
            return obj2.bar(123) + self.a2 + b2;
        }
    };

    return obj1.foo(50);
}

function checkClosuresWithObjectConstrExprInReturnStmt(int b1) returns object {
    int a2;
    function foo(int b2) returns int;
} {
    final int a1 = 10;

    return object {
        int a2 = 20 + b1;
        function foo(int b2) returns int {
            var obj2 = object {
                int a3 = a1;
                int a4 = 10;
                function bar(int b3) returns int {
                    return a1 + self.a3 + self.a4 + b3 + intVal;
                }
            };
            return obj2.bar(123) + self.a2 + b2;
        }
    };
}

function checkClosuresWithClientObjectConstrExpr(int b1) returns int {
    final int a1 = 10;

    var obj1 = client object {
        int a2 = 10;
        remote function bar(int b2) returns int {
            return a1 + self.a2 + b1 + b2 + intVal;
        }
    };
    return obj1->bar(10);
}

function checkClosuresWithObjectConstrExprInClientObjectConstrExpr(int b1) returns int {
    final int a1 = 10;

    var obj1 = client object {
        private int a2;
        private int a3;
        object {
            int a3;
            function foo(int b2) returns int;
        } obj2 = object {
            int a3 = 10;
            function foo(int b2) returns int {
                return self.a3 + b2;
            }
        };
        function init() {
            self.a2 = 10;
            self.a3 = 20;
        }
        remote function bar(int b2) returns int {
            int a2 = self.a2 + b1 + b2;
            var obj2 = object {
                int a3 = 40;
                function foo(int b3) returns int {
                    int sum = a1 + a2 + b3;
                    return sum;
                }
            };
            return obj2.foo(50) + self.a2 + self.obj2.foo(20);
        }
    };

    return obj1->bar(10);
}

function checkClosuresWithServiceObjectConstrExpr(int b1) returns int {
    final int a1 = 10;

    var obj1 = isolated service object {
        private final int a2;
        private final int a3;
        private object {
            int a3;
            isolated function foo(int b2) returns int;
        } obj2 = object {
            int a3 = 10;
            isolated function foo(int b2) returns int {
                return self.a3 + b2;
            }
        };

        function init() {
            self.a2 = 10;
            self.a3 = 20;
        }

        isolated remote function bar() returns int {
            final int a2 = self.a2 + b1;
            var obj2 = object {
                int a3 = 40;
                isolated function foo(int b3) returns int {
                    int sum = a1 + a2 + b3;
                    return sum;
                }
            };
            lock {
                return obj2.foo(50) + self.a2 + self.obj2.foo(20);
            }
        }

        isolated resource function get foo() returns int {
            final int a2 = self.a2 + b1 + a1;
            var obj2 = object {
                int a3 = 10;
                isolated function foo(int b3) returns int {
                    return b3;
                }
            };
            lock {
                return obj2.foo(50) + self.a2 + self.obj2.foo(20) + a2;
            }
        }
    };

    var val = wait callMethod(obj1, "$get$foo");
    if val is int {
        var val2 = wait callMethod(obj1, "bar");
        if (val2 is int) {
            return val + val2;
        }
    }
    return 0;
}

type Foo object {
    int i;
    function foo(int b2) returns int;
};

type Bar object {
    function bar(int b3) returns object {};
};

function checkClosuresWithObjectConstrExprsInObjectConstrExpr(int b1) returns int {
    final int a1 = 10;

    var obj1 = object Foo {
        int i;

        function init() {
            self.i = b1 + a1;
        }

        function foo(int b2) returns int {
            Bar obj2 = object {
                function bar(int b3) returns object {
                    int i;
                    function foo(int b4) returns int;
                } {
                    return object {
                        int i = 10;
                        function foo(int b4) returns int {
                            return intVal + b4;
                        }
                    };
                }
            };
            Foo obj3 = <Foo>obj2.bar(10);
            return a1 + self.i + b1 + obj3.foo(50);
        }
    };

    return obj1.foo(20);
}

function checkClosuresWithObjectConstrExprAsArrayMember(int b1, string str) returns int {
    final int a1 = 10;

    var obj1 = object {string j = str; boolean k = false;};
    Foo[] arr = [
        object {
            int i;

            function init() {
                self.i = b1 + a1;
            }

            function foo(int b2) returns int {
                Bar obj2 = object {
                    function bar(int b3) returns object {} {
                        return object {string j = str; boolean k = true;};
                    }
                };
                object {string j; boolean k;} obj3 = <object {string j; boolean k;}>obj2.bar(10);
                if obj1.k != obj3.k {
                    self.i = 200;
                }
                return b1 + a1 + self.i + b2 + intVal;
            }
        }
    ];

    return arr[0].foo(10);
}

const number = 10;

function checkClosuresWithObjectConstrExprInEqaulityExpr(int b1) returns int {
    final int a1 = 100;

    var obj1 = object {
        function bar(int b3) returns int {
            if object {
                int i = number;
                function foo(int b2) returns int {
                    return self.i + b1;
                }
            } === object {
                int i = number;
                function foo(int b2) returns int {
                    return self.i + b2;
                }
            } {
                return a1;
            }
            return b1 + b3 + a1;
        }
    };

    return obj1.bar(10);
}

function testClosuresWithObjectConstrExpr() {
    assertValueEquality("CACBC", checkClosuresWithObjectConstrExpr("C"));
}

function testClosuresWithObjectConstrExprAsFunctionDefaultParam() {
    assertValueEquality("ABAABAC", checkClosuresWithObjectConstrExprAsFunctionDefaultParam());
    assertValueEquality("ABBBCAB", checkClosuresWithObjectConstrExprAsFunctionDefaultParam(object {
        string x = B;
        function foo(string c) returns string {
            return self.x + c + A + B;
        }
    }));
}

function testClosuresWithObjectConstrExprInAnonFunc() {
    function () returns int func1 = checkClosuresWithObjectConstrExprInAnonFunc();
    assertValueEquality(160, func1());
}

function testClosuresWithObjectConstrExprInObjectFunc() {
    var res = checkClosuresWithObjectConstrExprInObjectFunc(10);
    assertValueEquality(true, res is int);
    if res is int {
        assertValueEquality(183, res);
    }
}

function testClosuresWithObjectConstrExprInVarAssignment() {
    assertValueEquality(273, checkClosuresWithObjectConstrExprInVarAssignment(20));
}

function testClosuresWithObjectConstrExprInReturnStmt() {
    object {
        int a2;
        function foo(int b2) returns int;
    } obj1 = checkClosuresWithObjectConstrExprInReturnStmt(10);
    assertValueEquality(283, obj1.foo(10) + obj1.a2);
}

function testClosuresWithClientObjectConstrExpr() {
    assertValueEquality(100, checkClosuresWithClientObjectConstrExpr(10));
}

function testClosuresWithObjectConstrExprInClientObjectConstrExpr() {
    assertValueEquality(130, checkClosuresWithObjectConstrExprInClientObjectConstrExpr(10));
}

function testClosuresWithServiceObjectConstrExpr() {
    assertValueEquality(240, checkClosuresWithServiceObjectConstrExpr(10));
}

function testClosuresWithObjectConstrExprsInObjectConstrExpr() {
    assertValueEquality(150, checkClosuresWithObjectConstrExprsInObjectConstrExpr(10));
}

function testClosuresWithObjectConstrExprAsArrayMember() {
    assertValueEquality(290, checkClosuresWithObjectConstrExprAsArrayMember(10, "A"));
}

function testClosuresWithObjectConstrExprInEqaulityExpr() {
    assertValueEquality(120, checkClosuresWithObjectConstrExprInEqaulityExpr(10));
}

public function callMethod(service object {} s, string name) returns future<any|error> = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "callMethod"
} external;

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
