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

// assertion helpers

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actual.toString () + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'false', found '" + actual.toString () + "'");
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
