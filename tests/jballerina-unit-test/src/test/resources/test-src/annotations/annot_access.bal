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

import ballerina/lang.'object as lang;

type Annot record {
    string foo;
    int bar?;
};

public annotation Annot v1 on type, class;
annotation Annot[] v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation map<string> v5 on resource function;
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
    resource function res(@v6 { foo: "v64" } int intVal) returns @v7 () {
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

service serTwo = @v8 {
                     foo: "v82"
                 } service {

    @v5 {
        val: "542"
    }
    resource function res(@v6 { foo: "v642" } int intVal) returns @v7 error? {
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
    *lang:Listener;

    public function init() {
    }

    public function __attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }
}

// TODO: #17936
//public function testInlineAnnotAccess() returns boolean {
//    Annot? f = (typeof a).@v1;
//    return f is Annot;
//}
