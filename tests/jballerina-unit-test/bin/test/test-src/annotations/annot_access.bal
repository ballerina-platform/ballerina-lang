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

function testListExprInConstAnnot() {
    EntityConfig? annot = MedicalNeed.@Entity;
    assertTrue(annot is EntityConfig);

    EntityConfig config = <EntityConfig> annot;
    assertEquality(["key1", "key2"], config.key);
}

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
}
