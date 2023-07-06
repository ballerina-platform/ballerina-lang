// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

int x = 1;

class ClassWithDefaultValueAccessingMutableVar {
    int i = x;

    function init() {
    }
}

service object {} b = service object {
    private int i = x;

    function init() {
    }
};

function nonIsolatedFunc() returns int => x;

class ClassWithDefaultValueCallingNonIsolatedFunction {
    private int i = nonIsolatedFunc();

    function init() {
        self.i = 0;
    }
}

class ClassWithNonIsolatedNewExprForNonIsolatedInitAsDefaultValue {
    private object {} ob = new ClassWithDefaultValueCallingNonIsolatedFunction();

    function init() {
    }
}

class ClassWithInitMethodWithWorkers {
    int i = 0;

    function init() {
        worker w {

        }
    }
}

final int y = 2;

class ClassWithDefaultValueAccessingImmutableVar {
    int i = y;

    function init() {
    }
}

service object {} g = service object {
    private int i = y;

    function init() {
    }
};

function isolatedFunc() returns int => y;

class ClassWithDefaultValueCallingIsolatedFunction {
    private int i = isolatedFunc();

    function init() {
        self.i = 0;
    }
}

class ClassWithIsolatedNewExprForIsolatedInitAsDefaultValue {
    private object {} ob = new ClassWithDefaultValueCallingIsolatedFunction();

    function init() {
    }
}

class ClassWithIsolatedInit {
    int i = 0;

    function init() {
    }
}

function testIsolatedInference() {
    ClassWithDefaultValueAccessingMutableVar a = new;
    assertFalse(<any> a is isolated object {});
    assertFalse(isMethodIsolated(a, "init"));

    assertTrue(<any> b is isolated object {});
    // https://github.com/ballerina-platform/ballerina-lang/issues/31371
    // assertFalse(isMethodIsolated(b, "init"));

    ClassWithDefaultValueCallingNonIsolatedFunction c = new;
    assertTrue(<any> c is isolated object {});
    assertFalse(isMethodIsolated(c, "init"));

    ClassWithNonIsolatedNewExprForNonIsolatedInitAsDefaultValue d = new;
    assertTrue(<any> d is isolated object {});
    assertFalse(isMethodIsolated(d, "init"));

    ClassWithInitMethodWithWorkers e = new;
    assertFalse(<any> e is isolated object {});
    assertTrue(isMethodIsolated(e, "init"));

    ClassWithDefaultValueAccessingImmutableVar f = new;
    assertFalse(<any> f is isolated object {});
    assertTrue(isMethodIsolated(f, "init"));

    assertTrue(<any> g is isolated object {});
    // https://github.com/ballerina-platform/ballerina-lang/issues/31371
    // assertTrue(isMethodIsolated(g, "init"));

    ClassWithDefaultValueCallingIsolatedFunction h = new;
    assertTrue(<any> h is isolated object {});
    assertTrue(isMethodIsolated(h, "init"));

    ClassWithIsolatedNewExprForIsolatedInitAsDefaultValue i = new;
    assertTrue(<any> i is isolated object {});
    assertTrue(isMethodIsolated(i, "init"));

    ClassWithIsolatedInit j = new;
    assertFalse(<any> j is isolated object {});
    assertTrue(isMethodIsolated(j, "init"));
}

isolated function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

isolated function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

isolated function isMethodIsolated(object {}|typedesc val, string methodName) returns boolean = @java:Method {
                                            'class: "org.ballerinalang.test.isolation.IsolationInferenceTest",
                                            paramTypes: ["java.lang.Object", "io.ballerina.runtime.api.values.BString"]
                                        } external;
