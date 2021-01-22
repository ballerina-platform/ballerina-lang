// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.'value;

function getValue(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function testParamToLHS() {
    string s = getValue(int);

    int i = getValue(float);
    i = getValue(decimal);
    i = getValue(string);
    i = getValue(boolean);

    boolean b = getValue(int);

    float f = getValue(int);
}

function testIncompatibleArg() {
    json j = getValue(json);
}

function getMap(typedesc<anydata>... aTypeVar) returns map<aTypeVar> = @java:Method {
    'class: "xyz.pubudu.Hello",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function testRestParamReference() {
    map<int> m = getMap(int, float);
}

function funcWithDefaultableParam(typedesc<anydata> td = customType) returns td = @java:Method {
    'class: "xyz.pubudu.Hello",
    name: "funcWithDefaultableParam",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function testDefaultableParamRef() {
    int i = funcWithDefaultableParam();
    float f = funcWithDefaultableParam();
}

function getRecord(typedesc<anydata> td) returns record {| string name; td misc; |} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getRecord",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getObject(typedesc<anydata> td) returns object {
                                                     string name;
                                                     td misc;
                                                 } = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getObject",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getError(typedesc<string> reason, typedesc<record {| (value:Cloneable)...; |}> detail,
                    error<record {| string message?; error cause?; (value:Cloneable)...; |}> err)
                                                                    returns error<detail> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getError",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc",
                    "io.ballerina.runtime.api.values.BError"]
} external;

function foo(typedesc<anydata> td = string, td s = "foo") returns td {
    return "foo";
}

function getNonTypedescExpr(typedesc<anydata> aTypeVar = getTypedesc()) returns aTypeVar = @java:Method {
    'class: "xyz.pubudu.Hello",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTypedesc() returns typedesc<anydata> {
    return float;
}

function referToANonExistingParam(typedesc<anydata> aTypeVar = int) returns NonExistentParam = @java:Method {
    'class: "xyz.pubudu.Hello",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function referToNonTypedescParam(string str) returns str = @java:Method {
    'class: "xyz.pubudu.Hello",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function normalFunction(string str) returns str {
    return "foo";
}

function getValue2(typedesc<int|string> aTypeVar) returns aTypeVar = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function testInvalidFunctionAssignment() {
    function (typedesc<string|int> td) returns string fn1 = getValue2;
    function (typedesc<string|int> td) returns td fn2 = getValue2;
}

type customType int|float;

public type OutParameter object {
    // This is OK, referencing classes/object constructors should have 'external' implementations.
    public isolated function get(typedesc<anydata> td) returns td|error;
};

public class OutParameterClass {
    *OutParameter;

    public isolated function get(typedesc<anydata> td) returns td|error => error("oops!"); // error
}

var OutParameterObject = object OutParameter {
    public isolated function get(typedesc<anydata> td) returns td|error => error("oops!"); // error
};

public class Bar {
    public function get(typedesc<anydata> td) returns td|error = external;
}

public class Baz {
    public function get(typedesc<anydata> td) returns anydata|error = external;
}

public class Qux {
    public function get(typedesc<anydata> td) returns td|error = external;
}

public class Quux {
    public function get(typedesc<any> td) returns td|error = external;
}

public class Quuz {
    public function get(typedesc<int|string> td) returns td|error = external;
}

class Corge {
    function get(typedesc<anydata> td, typedesc<anydata> td2) returns td2|error = external;
}

class Grault {
    function get(typedesc<anydata> td, typedesc<anydata> td2) returns td|error = external;
}

public function testSubtypingAgainstConcreteReturnType() {
    Bar bar = new Baz();
    Baz baz = new Bar(); // OK
    Bar bar2 = new Qux(); // OK

    Quux quux = new Qux();
    Qux qux = new Quux();
    Baz baz2 = new Quux();
    Quuz quuz = new Qux();

    Corge corge = new Grault();
    Grault grault = new Corge();
}
