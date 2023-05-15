// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// test object field access

class MO {
    int x = 0;
}

class MOO {
    *MO;
    int n = 0;

    public function init() {
        self.x = 5;
    }
}

var objCreatedViaConstructor = object MOO {
    int n = 20;
    int y = 5;

    public function init() {
        self.x = 4;
        self.y = 10;
    }
};

function testObjectCreationViaObjectConstructor() {
    assertValueEquality(20, objCreatedViaConstructor.n);
    assertValueEquality(4, objCreatedViaConstructor.x);
    assertValueEquality(10, objCreatedViaConstructor.y);
}

// Test annotation attachment and access

public type ObjectData record {|
    string descriptor = "";
|};

public annotation ObjectData OBJAnnots on class;

var obj = @OBJAnnots { descriptor: "ConstructedObject" }
    object {
        int n = 0;
        function inc() {
            self.n += 1;
        }
    };

function testObjectConstructorAnnotationAttachment() {
    typedesc<object{}> t = typeof obj;
    ObjectData annotationVal = <ObjectData>t.@OBJAnnots;
    assertValueEquality("ConstructedObject", annotationVal.descriptor);
}

function testObjectConstructorObjectFunctionInvocation() {
    obj.inc();
    assertValueEquality(1, obj.n);
    obj.inc();
    assertValueEquality(2, obj.n);
}

//var remoteObject = @OBJAnnots { descriptor: "ConstructedObject" }
//    client object {
//        int n = 0;
//        remote function remoteFunc() {
//            self.n += 1;
//        }
//
//        function inc() {
//            self.n += 1;
//        }
//    };
//
//function testObjectConstructorClientKeyword() {
//    remoteObject->remoteFunc();
//    assertTrue(remoteObject.n == 1);
//    remoteObject.inc();
//    assertTrue(remoteObject.n == 2);
//}

class MoAdvanced {
    int n = 0;

    public function setN(int userN) {
        self.n = userN;
    }

    public function init() {
        self.n = 0;
    }
}

// test function methods

function testObjectConstructorIncludedMethod() {
    var objWithIncludedMethod = object MoAdvanced {

        public function init() {
            self.n = -1;
        }

        public function setN(int userN) {
            self.n = userN;
        }
    };

    objWithIncludedMethod.setN(200);
    assertTrue(objWithIncludedMethod.n == 200);
    objWithIncludedMethod.setN(100);
    assertTrue(objWithIncludedMethod.n == 100);
}

// test distinct id support

distinct class DistinctFoo {
    int i = 0;
}

distinct class DistinctFooA {
    int i = 0;
}

distinct class DistinctFooB {
    int i = 0;
}

function testObjectConstructorWithDistinctExpectedType() {
    DistinctFoo distinctObject = object {
                                        int i;
                                        function init() {
                                            self.i = 20;
                                        }
                                    };
    assertValueEquality(20, distinctObject.i);
}

function testObjectConstructorWithDistinctTypeReference() {
    DistinctFooA distinctObject = object DistinctFooA {
                                            int i;
                                            function init() {
                                                self.i = 30;
                                            }
                                         };
    assertValueEquality(30, distinctObject.i);
}

function testObjectConstructorWithDistinctTypeReferenceVar() {
    var distinctObject = object DistinctFooB {
                                            int i;
                                            function init() {
                                                self.i = 25;
                                            }
                                         };
    DistinctFooB newDistinctRef = distinctObject;
    assertValueEquality(25, newDistinctRef.i);
}

function testObjectConstructorWithDefiniteTypeAndWithoutReference() {
    DistinctFooA|DistinctFooA distinctObject = object {
                                        int i;
                                        function init() {
                                            self.i = 20;
                                        }
                                    };
    DistinctFooA newDistinctRef = distinctObject;
    assertValueEquality(20, newDistinctRef.i);
}

//////  Test object-constructor-expr with `readonly` contextually expected type //////

readonly class ReadOnlyClass {
    int a;
    string[] b;

    function init(int a, string[] & readonly b) {
        self.a = a;
        self.b = b;
    }
}

type Object object {
    int a;
    string[] b;
};

final string[] & readonly immutableStringArr = [];

function testObjectConstructorExprWithReadOnlyCET() {
    ReadOnlyClass v1 = object {
        int a = 1;
        string[] b = immutableStringArr;
    };
    Object ob1 = v1;
    assertTrue(<any> v1 is readonly);
    validateInvalidUpdateErrors(ob1);

    ReadOnlyClass v2 = object {
        int a;
        string[] b;

        function init() {
            self.a = 2;
            self.b = [];
        }
    };
    validateInvalidUpdateErrors(v2);
    assertTrue(<any> v2 is readonly);

    Object & readonly v3 = object {
        int a = 1;
        string[] b = immutableStringArr;
        stream<string>? c = ();
    };
    validateInvalidUpdateErrors(v3);
    assertTrue(<any> v3 is readonly);

    Object & readonly v4 = object {
        int a = 1;
        string[] b;
        stream<string>? c;

        function init() {
            self.b = immutableStringArr;
            self.c = ();
        }
    };
    validateInvalidUpdateErrors(v4);
    assertTrue(<any> v4 is readonly);

    readonly & object {
        string[] a;
        stream<string>? b;
    } v5 = object {
        string[] a = immutableStringArr;
        stream<string>? b;

        function init() {
            self.b = ();
        }
    };

    object {
        string[] a;
        stream<string>? b;
    } ob5 = v5;

    var fn = function () {
        ob5.b = ();
    };

    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertValueEquality("{ballerina/lang.object}InherentTypeViolation", err.message());
    assertValueEquality("modification not allowed on readonly value", <string> checkpanic err.detail()["message"]);

    assertTrue(<any> v5 is readonly);
}

function validateInvalidUpdateErrors(Object ob) {
    validateInvalidUpdateErrorForFieldA(ob);
    validateInvalidUpdateErrorForFieldB(ob);
}

function validateInvalidUpdateErrorForFieldA(Object ob) {
    var fn = function () {
        ob.a = 2;
    };

    error? res = trap fn();
    assertTrue(res is error);

    error err = <error> res;
    assertValueEquality("{ballerina/lang.object}InherentTypeViolation", err.message());
    assertValueEquality("modification not allowed on readonly value", <string> checkpanic err.detail()["message"]);
}

function validateInvalidUpdateErrorForFieldB(Object ob) {
    var fn1 = function () {
        ob.b[0] = "foo";
    };

    error? res1 = trap fn1();
    assertTrue(res1 is error);

    error err1 = <error> res1;
    assertValueEquality("{ballerina/lang.array}InvalidUpdate", err1.message());
    assertValueEquality("modification not allowed on readonly value", <string> checkpanic err1.detail()["message"]);

    var fn2 = function () {
        ob.b = [];
    };

    error? res2 = trap fn2();
    assertTrue(res2 is error);

    error err2 = <error> res2;
    assertValueEquality("{ballerina/lang.object}InherentTypeViolation", err2.message());
    assertValueEquality("modification not allowed on readonly value", <string> checkpanic err2.detail()["message"]);
}

type Bar readonly & object {
    int i;
};

function testObjectConstructorWithReferredIntersectionType() {
    Bar b = object {
        int i = 1;
        int j = 2;
    };
    assertTrue(b is readonly & object {int i; int j;});
    assertValueEquality(1, b.i);
}

var obj1 = object {
    function foo() returns boolean {
        var intOrNil = self.bar();
        return intOrNil is int;
    }

    function bar() returns int? {
        return 2;
    }
};

public type DistinctError distinct error;

var obj2 = object {
    function foo() returns boolean {
        var err = self.bar();
        return err is DistinctError;
    }

    function bar() returns DistinctError? {
        return error("MSG");
    }
};

var obj3 = object {
    function foo() returns boolean {
        var err1 = self.bar1();
        var err2 = self.bar2();
        return (err1 is DistinctError) && (err2 is DistinctError);
    }

    function bar1() returns DistinctError? {
        return error("MSG1");
    }

    function bar2() returns DistinctError? {
        return error("MSG2");
    }
};

function testMultipleVarAssignments() {
    assertTrue(obj1.foo());
    assertTrue(obj2.foo());
    assertTrue(obj3.foo());
}

function testLocalVariablesAsFieldDefaults() {
    int a = 10;

    var obj = object {
        int x = a;

        function init() {
        }
    };
    assertValueEquality(obj.x, 10);
}

int b = 11;

var moduleObj = object {
    int x = b;

    function init() {
    }
};

function testModuleLevelObjectCtrWithModuleLevelVariableAsFieldDefaults() {
    assertValueEquality(moduleObj.x, 11);
    b = 12;
    assertValueEquality(moduleObj.x, 11);
}

// assertion helpers

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    string actualValAsString = "";
    if (actual is error) {
        actualValAsString = actual.toString();
    } else {
        actualValAsString = actual.toString();
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actualValAsString + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    string actualValAsString = "";
    if (actual is error) {
        actualValAsString = actual.toString();
    } else {
        actualValAsString = actual.toString();
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'false', found '" + actualValAsString + "'");
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if isEqual(actual, expected) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

isolated function isEqual(anydata|error actual, anydata|error expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else {
        return (actual === expected);
    }
}
