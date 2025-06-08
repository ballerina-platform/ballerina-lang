// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type, class;
annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation map<string> v5 on object function;
annotation Annot v6 on parameter;
public annotation v7 on return;
annotation Annot[] v8 on service;

string strValue = "v1 value";

@v1 {
    foo: strValue,
    bar: 1
}
public type T1 record {
    string name;
};

T1 a = { name: "John" };

function testTypeAnnotAccess1() returns boolean {
    typedesc<any> t = typeof a;
    Annot? annot = t.@v1;
    return annot is Annot && annot.foo == strValue && annot["bar"] == 1;
}

function testTypeAnnotAccess2() returns boolean {
    T1 b = { name: "John" };
    typedesc<any> t = typeof b;
    Annot[]? annot = t.@v2;
    return annot is ();
}

@v1 {
    foo: strValue
}
@v2 {
    foo: "v2 value 1"
}
@v2 {
    foo: "v2 value 2"
}
class T2 {
    string name = "ballerina";

    @v3 {
        foo: "v31 value"
    }
    @v4 {
        foo: 41
    }
    public function setName(@v6 { foo: "v61 value required" } string name,
                            @v6 { foo: "v61 value defaultable" } int id = 0,
                            @v6 { foo: "v61 value rest" } string... others) returns @v7 () {
        self.name = name;
    }

    @v3 {
        foo: "v32 value"
    }
    @v4 {
        val: 42
    }
    public function getLetter(@v6 { foo: "v62 value" } int intVal) returns @v7 string {
        return self.name.substring(intVal, intVal + 1);
    }
}

function testObjectTypeAnnotAccess1() returns boolean {
    T2 c = new;
    typedesc<any> t = typeof c;
    Annot? annot = t.@v1;
    return annot is Annot && annot.foo == strValue;
}

T2 d = new;

function testObjectTypeAnnotAccess2() returns boolean {
    typedesc<any> t = typeof d;
    Annot[]? annots = t.@v2;
    if (annots is Annot[]) {
        if (annots.length() != 2) {
            return false;
        }
        Annot annot1 = annots[0];
        Annot annot2 = annots[1];
        return annot1.foo == "v2 value 1" && annot2.foo == "v2 value 2";
    }
    return false;
}

function testObjectTypeAnnotAccess3() returns boolean {
    T2 e = new;
    typedesc<any> t = typeof e;
    Annot? annot = t.@v3;
    return annot is ();
}

listener Listener lis = new;

string v8a = "v8a";

type ser service object {
};

@v8 {
    foo: v8a
}
@v8 {
    foo: "v8b"
}
service ser on lis {

    @v3 {
        foo: "v34"
    }
    @v5 {
        val: "54"
    }
    resource function get res(@v6 { foo: "v64" } int intVal) returns @v7 () {
        return;
    }
}

function testServiceAnnotAccess1() returns boolean {
    typedesc<any> t = typeof ser;
    Annot[]? annots = t.@v8;
    if (annots is Annot[]) {
        if (annots.length() != 2) {
            return false;
        }
        Annot annot1 = annots[0];
        Annot annot2 = annots[1];
        return annot1.foo == v8a && annot2.foo == "v8b";
    }
    return false;
}

function testServiceAnnotAccess2() returns boolean {
    typedesc<any> t = typeof ser;
    Annot? annot = t.@v1;
    return annot is ();
}

service object {} serTwo = @v8 {
                     foo: "v82"
                 } service object {

    @v5 {
        val: "542"
    }
    resource function get res(@v6 { foo: "v642" } int intVal) returns @v7 error? {
        return;
    }
};

function testServiceAnnotAccess3() returns boolean {
    typedesc<any> t = typeof serTwo;
    Annot[]? annots = t.@v8;
    if (annots is Annot[]) {
        if (annots.length() != 1) {
            return false;
        }
        Annot annot1 = annots[0];
        return annot1.foo == "v82";
    }
    return false;
}

function testServiceAnnotAccess4() returns boolean {
    typedesc<any> t = typeof serTwo;
    Annot[]? annot = t.@v2;
    return annot is ();
}

@v3 {
    foo: "func",
    bar: 1
}
function funcWithAnnots() {

}

function testFunctionAnnotAccess1() returns boolean {
    typedesc<any> t = typeof funcWithAnnots;
    Annot? annot = t.@v3;
    if (annot is Annot) {
        return annot.foo == "func" && annot?.bar == 1;
    }
    return false;
}

function testFunctionAnnotAccess2() returns boolean {
    typedesc<any> t = typeof serTwo;
    Annot[]? annot = t.@v2;
    return annot is ();
}

class Listener {
    public function init() {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }
}

public function testInlineAnnotAccess() returns boolean {
    Annot? f = (typeof a).@v1;
    return f is Annot;
}

type A record {|
    string val = "ABC";
|};

public annotation A v9 on function;

@v9
function myFunction1() {

}

function testAnnotWithEmptyMappingConstructor1() returns boolean {
    typedesc<any> t = typeof myFunction1;
    A? annot = t.@v9;
    if (annot is A) {
        return annot.val == "ABC";
    }
    return false;
}

public annotation map<string> v10 on function;

@v10
function myFunction2() {

}

function testAnnotWithEmptyMappingConstructor2() returns boolean {
    typedesc<any> t = typeof myFunction2;
    map<string>? annot = t.@v10;
    if (annot is map<string>) {
        return annot == {};
    }
    return false;
}

public annotation map<string>[] v11 on function;

@v11
@v11
function myFunction3() {

}

function testAnnotWithEmptyMappingConstructor3() returns boolean {
    typedesc<any> t = typeof myFunction3;
    map<string>[]? annot = t.@v11;
    if (annot is map<string>[]) {
        if (annot.length() != 2) {
            return false;
        }
        map<string> annot1 = annot[0];
        map<string> annot2 = annot[1];
        return annot1 == {} && annot2 == {};
    }
    return false;
}

public annotation A[] v12 on function;

@v12
@v12
function myFunction4() {

}

function testAnnotWithEmptyMappingConstructor4() returns boolean {
    typedesc<any> t = typeof myFunction4;
    A[]? annot = t.@v12;
    if (annot is A[]) {
        if (annot.length() != 2) {
            return false;
        }
        A annot1 = annot[0];
        A annot2 = annot[1];
        return annot1.val == "ABC" && annot2.val == "ABC";
    }
    return false;
}

annotation record {int i;} ObjMethodAnnot on function;

class MyClass {
    function foo(int i) returns string => i.toString();

    @ObjMethodAnnot {i: 101}
    function bar(int i) returns string => i.toString();
}

function testAnnotOnBoundMethod() {
    MyClass c = new;
    typedesc t1 = typeof c.foo;
    assertEquality((), t1.@ObjMethodAnnot);

    typedesc t2 = typeof c.bar;
    record {int i;}? r = t2.@ObjMethodAnnot;
    assertEquality(true, r is record {int i;});
    record {int i;} rec = <record {int i;}> r;
    assertEquality(101, rec.i);
}

public type EntityConfig record {|
    string[] key;
|};

public const annotation EntityConfig Entity on type;

@Entity {
    key: ["key1", "key2"]
}
type MedicalNeed record {

};

public type AnnotationRecord record {|
    string summary?;
    Examples examples?;
|};

public type Examples record {|
    map<ExampleItem> response;
|};

public type ExampleItem record {
    map<string> headers?;
};

const annotation AnnotationRecord annot on type;

@annot {
    examples: {
        response: {}
    }
}
type Employee record {|
    int id;
    string name;
|};

public type AnnotationRecord1 record {|
    string summary?;
    ExamplesReference examples?;
|};

type ExamplesReference Examples;

const annotation AnnotationRecord1 annot1 on type;

@annot1 {
    examples: {
        response: {}
    }
}
type Student record {|
    int id;
    string name;
|};

const Examples EXAMPLES = {
    response: {}
};

@annot {
    examples: EXAMPLES
}
type Teacher record {|
    int id;
    string name;
|};

function testConstTypeAnnotAccess() {
    Employee employee = {id: 1, name: "chirans"};
    typedesc<any> t = typeof employee;
    AnnotationRecord? annot = t.@annot;
    assertTrue(annot is AnnotationRecord);
    AnnotationRecord config = <AnnotationRecord> annot;
    assertEquality({"response":{}}, config.examples);
    assertTrue(config.examples is readonly);

    Student student = {id: 2, name: "sachintha"};
    typedesc<any> s = typeof student;
    AnnotationRecord1? annot1 = s.@annot1;
    assertTrue(annot1 is AnnotationRecord1);
    AnnotationRecord1 config1 = <AnnotationRecord1> annot1;
    assertEquality({"response":{}}, config1.examples);
    assertTrue(config1.examples is readonly);

    Teacher teacher = {id: 1, name: "James"};
    typedesc<any> t1 = typeof teacher;
    AnnotationRecord? annot2 = t1.@annot;
    assertTrue(annot2 is AnnotationRecord);
    AnnotationRecord config2 = <AnnotationRecord> annot2;
    assertEquality({"response":{}}, config2.examples);
    assertTrue(config2.examples is readonly);
}

function testListExprInConstAnnot() {
    EntityConfig? annot = MedicalNeed.@Entity;
    assertTrue(annot is EntityConfig);

    EntityConfig config = <EntityConfig> annot;
    assertEquality(["key1", "key2"], config.key);
}

annotation record {| string name; |} ObjectAnnot on type;
annotation MethodAnnot on function;

@ObjectAnnot {
    name: "ObjectTypeWithAnnots"
}
type ObjectTypeWithAnnots object {
    @MethodAnnot
    function getInt() returns int;
};

type ObjectTypeWithoutAnnots object {
    function getInt() returns int;
};

function testObjectTypeAnnotations() {
    record {|string name;|}? objectAnnot = ObjectTypeWithAnnots.@ObjectAnnot;
    assertTrue(objectAnnot !is ());
    record {|string name;|} objectAnnotValue = <record {|string name;|}> objectAnnot;
    assertEquality("ObjectTypeWithAnnots", objectAnnotValue.name);

    anydata methodAnnot = getMethodAnnotations(ObjectTypeWithAnnots, "getInt", "MethodAnnot");
    assertTrue(methodAnnot);

    record {|string name;|}? objectAnnot2 = ObjectTypeWithoutAnnots.@ObjectAnnot;
    assertTrue(objectAnnot2 is ());

    anydata methodAnnot2 = getMethodAnnotations(ObjectTypeWithoutAnnots, "getInt", "MethodAnnot");
    assertTrue(methodAnnot2 is ());
}

annotation ServiceTypeAnnot on type, class;
annotation Kind ServiceTypeMethodAnnot on function;

@ServiceTypeAnnot
public type S1 service object {

    @ServiceTypeMethodAnnot {
        kind: "resource"
    }
    resource function get res() returns string;

    @ServiceTypeMethodAnnot {
        kind: "remote"
    }
    remote function rem() returns string;

    @ServiceTypeMethodAnnot {
        kind: "normal"
    }
    function fn() returns int;

    resource function get res2() returns string;
};

public type S2 service object {

    resource function get res() returns string;

    remote function rem() returns string;

    function fn() returns int;

    @ServiceTypeMethodAnnot {
        kind: "remote 2"
    }
    remote function rem2() returns string;
};

type Kind record {|
    string kind;
|};

function testServiceObjectTypeAnnotations() {
    true? serviceObjectAnnot = S1.@ServiceTypeAnnot;
    assertTrue(serviceObjectAnnot);

    anydata methodAnnot = getResourceMethodAnnotations(S1, "get", ["res"], "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is Kind);
    Kind kind = <Kind> methodAnnot;
    assertEquality("resource", kind.kind);

    methodAnnot = getRemoteMethodAnnotations(S1, "rem", "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is Kind);
    kind = <Kind> methodAnnot;
    assertEquality("remote", kind.kind);

    methodAnnot = getMethodAnnotations(S1, "fn", "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is Kind);
    kind = <Kind> methodAnnot;
    assertEquality("normal", kind.kind);

    methodAnnot = getResourceMethodAnnotations(S1, "get", ["res2"], "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is ());

    serviceObjectAnnot = S2.@ServiceTypeAnnot;
    assertTrue(serviceObjectAnnot is ());

    methodAnnot = getResourceMethodAnnotations(S2, "get", ["res"], "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is ());

    methodAnnot = getRemoteMethodAnnotations(S2, "rem", "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is ());

    methodAnnot = getMethodAnnotations(S2, "fn", "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is ());

    methodAnnot = getRemoteMethodAnnotations(S2, "rem2", "ServiceTypeMethodAnnot");
    assertTrue(methodAnnot is Kind);
    kind = <Kind> methodAnnot;
    assertEquality("remote 2", kind.kind);
}

type ToolConfig record {|
    string name?;
|};

annotation ToolConfig Tool on function;

class Agent {
    private (isolated function)[] tools = [];

    isolated function init((isolated function)[] tools) {
        self.tools = tools;
    }

    isolated function run() returns ToolConfig|error {
        typedesc<isolated function> typedesVal = typeof self.tools.pop();
        ToolConfig toolConfig = check typedesVal.@Tool.ensureType();
        return toolConfig;
    }
}

class Agent2 {
    private ToolConfig toolConfig;

    isolated function init((isolated function)[] tools) returns error? {
        typedesc<isolated function> typedesVal = typeof tools.pop();
        ToolConfig toolConfig = check typedesVal.@Tool.ensureType();
        self.toolConfig = toolConfig;
    }

    isolated function run() returns ToolConfig {
        return self.toolConfig;
    }
}

@Tool {name: n}
isolated function sum(int a, int b) returns int {
    return a + b;
}

string? res1 = (typeof sum).@Tool?.name;

Agent agent = new ([sum]);
Agent2|error agent2 = check new ([sum]);

public function testFuncAnnotationAccessInClasMethod() returns error? {
    ToolConfig|error toolConfig = agent.run();
    if toolConfig is ToolConfig {
        assertEquality(toolConfig.name, "name");
    } else {
        panic error("Agent run returned an error");
    }

    assertEquality(res1, "name");

    Agent2|error agentVar = agent2;
    if agentVar is Agent2 {
        assertEquality(agentVar.run().name, "name");
    } else {
        panic error("Agent init returned an error");
    }
}

string n = "name";

function getMethodAnnotations(typedesc<object {}> bTypedesc, string method, string annotName) returns anydata =
    @java:Method {
        'class: "org/ballerinalang/test/annotations/AnnotationRuntimeTest"
    } external;

function getRemoteMethodAnnotations(typedesc<service object {}> bTypedesc, string method, string annotName) returns anydata =
    @java:Method {
        'class: "org/ballerinalang/test/annotations/AnnotationRuntimeTest"
    } external;

function getResourceMethodAnnotations(typedesc<service object {}> bTypedesc, string method,
                                      string[] path, string annotName) returns anydata =
    @java:Method {
        'class: "org/ballerinalang/test/annotations/AnnotationRuntimeTest"
    } external;

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}
