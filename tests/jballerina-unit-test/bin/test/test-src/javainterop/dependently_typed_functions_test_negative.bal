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

function getWithDefaultableParams(int|string x, int|string y = 1, typedesc<int|string> z = int) returns z =
    @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getWithDefaultableParams"
    } external;

function testDependentlyTypedFunctionWithDefaultableParamsNegative() {
    string a = getWithDefaultableParams("");
    string b = getWithDefaultableParams("hello", z = int);
    int c = getWithDefaultableParams("hello", z = string);
    int d = getWithDefaultableParams(x = 1, z = string);
    int e = getWithDefaultableParams(x = 1, y = "", z = string);
    int f = getWithDefaultableParams(z = string, x = 1, y = "");
}

type IntOrString int|string;

public function testStartActionWithDependentlyTypedFunctionsNegative() {
    Client cl = new;

    future<int> a = start getWithUnion("", IntOrString);
    int|string|error b = start cl.get("", IntOrString);
    future<int|string> c = start cl->remoteGet("", IntOrString);

    future<int> d = start getWithUnion("hello", int);
    string e = start cl.get(3, string);
    future<string|error> f = start cl.get("");
}

function getWithUnion(int|string x, typedesc<int|string> y) returns y|error =
    @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getWithUnion"
    } external;

client class Client {
    function get(int|string x, typedesc<int|string> y = int) returns y|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "clientGetWithUnion"
    } external;

    remote function remoteGet(int|string x, typedesc<int|string> y) returns y|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "clientRemoteGetWithUnion"
    } external;
}

function getWithRestParam(int i, typedesc<int|string> j, int... k) returns j|boolean =
     @java:Method {
         'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
     } external;

function getWithMultipleTypedescs(int i, typedesc<int|string> j, typedesc<int> k, typedesc<int>... l)
    returns j|k|boolean = @java:Method { 'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType" } external;

function testArgsForDependentlyTypedFunctionViaTupleRestArgNegative() {
    [typedesc<string>] a = [string];
    int|error b = getWithUnion(123, ...a);

    [int, typedesc<int>] c = [10, int];
    string|error d = getWithUnion(...c);

    [int] e = [0];
    string|boolean f = getWithRestParam(...e);

    string|boolean g = getWithRestParam(1);

    [int, typedesc<string>] h = [101, string];
    int|boolean i = getWithRestParam(...h);

    [int, typedesc<int>, typedesc<int>] j = [1, int, int];
    string|boolean k = getWithMultipleTypedescs(...j);

    [typedesc<string>] l = [string];
    string|boolean m = getWithMultipleTypedescs(1, ...l);
}

function testArgsForDependentlyTypedFunctionViaArrayRestArgNegative() {
    typedesc<string>[1] a = [string];
    int|error b = getWithUnion(123, ...a);

    typedesc<int>[] c = [int];
    int|error d = getWithUnion(10, ...c);

    int[1] e = [1];
    var f = getWithUnion(...e);

    typedesc<int>[2] m = [int, int];
    string|boolean n = getWithMultipleTypedescs(1, string, ...m);

    typedesc<byte>[1] q = [byte];
    byte|boolean r = getWithMultipleTypedescs(1, ...q);
}

type XY record {|
    int|string x;
    typedesc<int> y = int;
|};

type IJ record {|
    int i;
    typedesc<string> j;
|};

type IJK record {|
    int i;
    typedesc<string> j;
    typedesc<int> k;
|};

function testArgsForDependentlyTypedFunctionViaRecordRestArgNegative() {
    XY a = {x: 1, y: int};
    string|error b = getWithUnion(...a);

    record {| typedesc<string> y; |} c = {y: string};
    int|error d = getWithUnion(123, ...c);

    IJ e = {i: 0, j: string};
    int|boolean f = getWithRestParam(...e);

    record {| |} g = {};
    string|boolean h = getWithRestParam(1, ...g);

    IJK i = {i: 1, j: string, k: int};
    string|boolean j = getWithMultipleTypedescs(...i);
}

public type ClientActionOptions record {|
    string mediaType?;
    string header?;
|};

public type TargetType typedesc<int|string>;

public client class ClientWithMethodWithIncludedRecordParamAndDefaultableParams {
    remote function post(TargetType targetType = int, *ClientActionOptions options)
        returns @tainted targetType = external;

    function calculate(int i, TargetType targetType = int, *ClientActionOptions options)
        returns @tainted targetType|error = external;
}

public client class ClientWithMethodWithIncludedRecordParamAndRequiredParams {
    remote function post(TargetType targetType, *ClientActionOptions options)
        returns @tainted targetType = external;
}

function testDependentlyTypedFunctionWithIncludedRecordParamNegative() {
    ClientWithMethodWithIncludedRecordParamAndDefaultableParams cl = new;
    int p1 = cl->post(mediaType = "application/json", header = "active", targetType = string);
    string p2 = cl->post(mediaType = "application/json", header = "active");
    string p3 = cl->post();
    string p4 = cl->post(targetType = int);
    int p5 = cl->post(targetType = string);
    int p6 = cl->post(mediaType = "application/json", header = "active", targetTypes = string);

    int p7 = cl.calculate(0, mediaType = "application/json", header = "active", targetType = string);
    string p8 = cl.calculate(1, mediaType = "application/json", header = "active");
    string p9 = cl.calculate(2);
    string p10 = cl.calculate(3, targetType = int);
    string p11 = cl.calculate(4, targetType = string);
    int p12 = cl.calculate(mediaType = "application/json", header = "active", targetType = string);
    string p13 = cl.calculate(5, targetType = string);
    int p14 = cl.calculate(6);
    string p15 = cl.calculate(0, mediaType = "application/json", header = "active", targetType = string);
    string p16 = cl.calculate(0, string, {});

    ClientWithMethodWithIncludedRecordParamAndRequiredParams cl2 = new;
    int p17 = cl2->post(mediaType = "application/json", header = "active", targetType = string);
    string p18 = cl2->post(mediaType = "application/json", header = "active");
    string p19 = cl2->post();
    string p20 = cl2->post(targetType = int);
    int p21 = cl2->post(targetType = string);
    int p22 = cl2->post(string, {});
    string p23 = cl2->post(int, {});
}

function functionWithInferredArgForParamOfTypeReferenceType(TargetType t = <>) returns t = external;

function testDependentlyTypedFunctionWithInferredArgForParamOfTypeReferenceTypeNegative() {
    boolean _ = functionWithInferredArgForParamOfTypeReferenceType();

    var _ = functionWithInferredArgForParamOfTypeReferenceType(boolean);
}
