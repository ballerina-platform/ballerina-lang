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

public type HSC record {|
    string hostRecField = "default_host_name";
    boolean boolRecField = true;
|};

public annotation HSC HSCfa on field;
public annotation HSC HSCsa on service;

function createService(string hosty, decimal maxAgeMy, boolean allowCredentials) returns service object {
    string xField;
} {

    var httpService =
    @HSCsa {
        hostRecField : hosty
    }
    isolated service object {

        final string xField = hosty;
    };

    return httpService;
}

public function testAnnotations() {
    var obj = createService("hostKRv", 200, true);
    assertValueEquality("hostKRv", obj.xField);

    typedesc<object {}> t = typeof obj;
    HSC annotationVal = <HSC>t.@HSCsa;

    assertValueEquality("hostKRv", annotationVal.hostRecField);

    obj = createService("hostKRv boom", 200, true);
    typedesc<object {}> t2 = typeof obj;
    HSC annotationVal2 = <HSC>t2.@HSCsa;
    assertValueEquality("hostKRv boom", annotationVal2.hostRecField);
}

public type ObjectData record {|
    string descriptor = "";
|};

public annotation ObjectData OBJAnnots on class;

function testObjectConstructorAnnotationAttachment() {
    final string constructed = "ConstructedObject";

    var obj = @OBJAnnots {
        descriptor: constructed
        }
    object {
        int n = 0;
        string state = constructed;
        function inc() {
            self.n += 1;
        }
    };
    typedesc<object {}> t = typeof obj;
    ObjectData annotationVal = <ObjectData>t.@OBJAnnots;
    assertValueEquality("ConstructedObject", annotationVal.descriptor);
    assertValueEquality("ConstructedObject", obj.state);
}

type Record record {|
    int[] x;
|};

annotation Record A on service;
annotation B on service;
annotation C on service;

function fn(int[] arr) returns object {} {
    return
    @A {
        x: arr
    }
    @B
    service object {

    };
}

function testAnnotsOfServiceObjectConstructorInReturnStmt() {
    int[] arr = [1, 2, 3];

    object {} ob = fn(arr);
    typedesc<object {}> td = typeof ob;

    Record? a = td.@A;
    assertTrue(a !is ());
    int[] annotArr = (<Record> a).x;
    assertTrue(annotArr == arr);
    assertValueEquality(annotArr, [1, 2, 3]);
    arr.push(4);
    assertValueEquality(arr, [1, 2, 3, 4]);
    assertValueEquality(annotArr, [1, 2, 3]);

    true? b = td.@B;
    assertTrue(b);

    true? c = td.@C;
    assertTrue(c is ());
}

const number = 10;

int intVal = 60;

type AnnotRecord record {|
    int x;
|};

annotation record {|int x;|} Config on service;

function checkClosuresWithObjectConstrExprWithAnnots(int a1) returns int {
    final int b = 30;
    object {
        int x;
        function foo(int c) returns int;
    } obj1 = @Config {
        x: a1 + intVal
    } service object {
        int x = a1 + b + number;
        function foo(int c) returns int {
            return a1 + b + self.x + c + intVal;
        }
    };

    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config;

    return obj1.foo(20) + obj1.x + annot.x;
}

annotation AnnotRecord Config2 on class;

function checkClosuresWithObjectConstrExprWithAnnotsAsFunctionDefaultParam(int b, object {
                                                                          int y;
                                                                          function foo(int c) returns int;
                                                                      } obj = @Config2 {
                                                                          x: intVal
                                                                      } object {
                                                                          int y = number;
                                                                          function foo(int c) returns int {
                                                                              return c + number;
                                                                          }
                                                                      }) returns int {
    return number + b + obj.y + obj.foo(20);
}

annotation record {|int x;|} Config3 on class;

function checkClosuresWithObjectConstrExprWithAnnotsInAnonFunc() returns (function () returns int) {
    return function() returns int {
        int a1 = 20;
        var obj1 = @Config3 {
            x: a1 + intVal
        } object {
            int a2 = 30;
            function foo(int a3) returns int {
                return a1 + self.a2 + a3 + intVal;
            }
        };
        AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config3;
        return obj1.foo(50) + annot.x;
    };
}

function checkClosuresWithObjectConstrExprWithAnnotsInObjectFunc(int b1) returns int {
    int a1 = 10;
    var obj1 = @Config3 {
        x: a1 + intVal + b1
    } object {
        int a2 = 20;
        function foo(int b2) returns int|error? {
            var obj2 = @Config3 {
                x: intVal
            } object {
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
            AnnotRecord annot = <AnnotRecord>(typeof obj2).@Config3;
            return obj2.bam(obj2.bar("123")) + self.a2 + annot.x;
        }
    };

    var res = obj1.foo(50);
    if res is int {
        AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config3;
        return res + annot.x;
    }
    return 0;
}

function checkClosuresWithObjectConstrExprWithAnnotsInVarAssignment(int b1) returns int {
    final int a1 = 10;
    object {
        int a2;
        function foo(int b2) returns int;
    } obj1;

    obj1 = @Config3 {
        x: intVal + a1 + b1
    } object {
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

    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config3;
    return obj1.foo(50) + annot.x;
}

function checkClosuresWithObjectConstrExprWithAnnotsInReturnStmt(int b1) returns
                                        object {
                                            int a2;
                                            function foo(int b2) returns int;
                                        } {
    final int a1 = 10;

    return @Config3 {
        x: a1 + intVal + b1
    } object {
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

function checkClosuresWithClientObjectConstrExprWithAnnots(int b1) returns int {
    final int a1 = 10;

    var obj1 = @Config3 {
        x: intVal + b1 + a1
    } client object {
        int a2 = 10;
        remote function bar(int b2) returns int {
            return a1 + self.a2 + b1 + b2 + intVal;
        }
    };

    int res = obj1->bar(10);
    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config3;
    return res + annot.x;
}

function checkClosuresWithObjectConstrExprWithAnnotsInClientObjectConstrExpr(int b1) returns int {
    final int a1 = 10;

    var obj1 = @Config3 {
        x: intVal + b1 + a1
    } client object {
        private int a2;
        private int a3;
        object {
            int a3;
            function foo(int b2) returns int;
        } obj2 = @Config3 {
            x: intVal
        } object {
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
            var obj2 = @Config3 {
                x: intVal
            } object {
                int a3 = 40;
                function foo(int b3) returns int {
                    int sum = a1 + a2 + b3;
                    return sum;
                }
            };
            AnnotRecord annot = <AnnotRecord>(typeof obj2).@Config3;
            return obj2.foo(50) + self.a2 + self.obj2.foo(20) + annot.x;
        }
    };
    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config3;
    int res = obj1->bar(10);
    return  res + annot.x;
}

function checkClosuresWithServiceObjectConstrExprWithAnnots(int b1) returns int {
    final int a1 = 10;

    var obj1 = @Config {
        x: intVal + b1 + a1
    } isolated service object {
        private final int a2 = 10;
        private final int a3;
        private object {
            int a3;
            isolated function foo(int b2) returns int;
        } obj2 = @Config2 {
            x: intVal
        } object {
            int a3 = 10;
            isolated function foo(int b2) returns int {
                return self.a3 + b2;
            }
        };

        function init() {
            self.a3 = 20;
        }

        isolated remote function bar() returns int {
            final int a2 = self.a2 + b1;
            var obj2 = @Config2 {
                x: intVal + a1 + b1 + a2
            } object {
                int a3 = 40;
                isolated function foo(int b3) returns int {
                    int sum = a1 + a2 + b3;
                    return sum;
                }
            };
            lock {
                AnnotRecord annot = <AnnotRecord>(typeof self.obj2).@Config2;
                return obj2.foo(50) + self.a2 + self.obj2.foo(20) + annot.x;
            }
        }

        isolated resource function get foo() returns int {
            final int a2 = self.a2 + b1 + a1 + self.a3;
            var obj2 = @Config2 {
                x: intVal
            } object {
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

    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config;
    return annot.x;
}

type Foo object {
    int i;
    function foo(int b2) returns int;
};

type Bar object {
    function bar(int b3) returns object {};
};

function checkClosuresWithObjectConstrExprWithAnnotsInObjectConstrExprWithAnnots(int b1) returns int {
    final int a1 = 10;

    var obj1 = @Config2 {
        x: intVal + a1 + b1
    } object Foo {
        int i;

        function init() {
            self.i = b1 + a1;
        }

        function foo(int b2) returns int {
            Bar obj2 = @Config3 {
                x: intVal
            } object {
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
            AnnotRecord annot = <AnnotRecord>(typeof obj2).@Config3;
            return a1 + self.i + b1 + obj3.foo(50) + annot.x;
        }
    };

    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config2;
    return obj1.foo(20) + annot.x;
}

function checkClosuresWithObjectConstrExprWithAnnotsAsArrayMember(int b1, string str) returns int {
    final int a1 = 10;

    var obj1 = object {string j = str; boolean k = false;};
    Foo[] arr = [
        @Config3 {
            x: intVal + a1 + b1
        } object {
            int i;

            function init() {
                self.i = b1 + a1;
            }

            function foo(int b2) returns int {
                Bar obj2 = @Config2 {
                    x: intVal
                } object {
                    function bar(int b3) returns object {} {
                        return object {string j = str; boolean k = true;};
                    }
                };
                object {string j; boolean k;} obj3 = <object {string j; boolean k;}>obj2.bar(10);
                if obj1.k != obj3.k {
                    self.i = 200;
                }
                AnnotRecord annot = <AnnotRecord>(typeof obj2).@Config2;
                return b1 + a1 + self.i + b2 + intVal + annot.x;
            }
        }
    ];

    return arr[0].foo(10);
}

function testClosuresWithObjectConstrExprWithAnnots() {
    assertValueEquality(290, checkClosuresWithObjectConstrExprWithAnnots(10));
}

function testClosuresWithObjectConstrExprWithAnnotsAsFunctionDefaultParam() {
    assertValueEquality(60, checkClosuresWithObjectConstrExprWithAnnotsAsFunctionDefaultParam(10));
    assertValueEquality(120, checkClosuresWithObjectConstrExprWithAnnotsAsFunctionDefaultParam(10, object {
        int y = 10;
        function foo(int c) returns int {
            return self.y + c + intVal;
        }
    }));
}

function testClosuresWithObjectConstrExprWithAnnotsInAnonFunc() {
    function () returns int func1 = checkClosuresWithObjectConstrExprWithAnnotsInAnonFunc();
    assertValueEquality(240, func1());
}

function testClosuresWithObjectConstrExprWithAnnotsInObjectFunc() {
    assertValueEquality(323, checkClosuresWithObjectConstrExprWithAnnotsInObjectFunc(10));
}

function testClosuresWithObjectConstrExprWithAnnotsInVarAssignment() {
    assertValueEquality(343, checkClosuresWithObjectConstrExprWithAnnotsInVarAssignment(10));
}

function testClosuresWithObjectConstrExprWithAnnotsInReturnStmt() {
    object {
        int a2;
        function foo(int b2) returns int;
    } obj1 = checkClosuresWithObjectConstrExprWithAnnotsInReturnStmt(10);
    AnnotRecord annot = <AnnotRecord>(typeof obj1).@Config3;
    assertValueEquality(363, obj1.foo(10) + obj1.a2 + annot.x);
}

function testClosuresWithClientObjectConstrExprWithAnnots() {
    assertValueEquality(180, checkClosuresWithClientObjectConstrExprWithAnnots(10));
}

function testClosuresWithObjectConstrExprWithAnnotsInClientObjectConstrExpr() {
   assertValueEquality(270, checkClosuresWithObjectConstrExprWithAnnotsInClientObjectConstrExpr(10));
}

function testClosuresWithServiceObjectConstrExprWithAnnots() {
    assertValueEquality(80, checkClosuresWithServiceObjectConstrExprWithAnnots(10));
}

function testClosuresWithObjectConstrExprWithAnnotsInObjectConstrExprWithAnnots() {
    assertValueEquality(290, checkClosuresWithObjectConstrExprWithAnnotsInObjectConstrExprWithAnnots(10));
}

function testClosuresWithObjectConstrExprWithAnnotsAsArrayMember() {
    assertValueEquality(350, checkClosuresWithObjectConstrExprWithAnnotsAsArrayMember(10, "A"));
}

type AssertionError distinct error;
const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(anydata actual) {
    assertValueEquality(actual, true);
}

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
