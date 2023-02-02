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
    string val;
};

public annotation Annot v1 on type, class;
annotation Annot v2 on class;
public annotation Annot v3 on function;
annotation map<int> v4 on object function;
public annotation Annot v5 on object function;
annotation Annot v6 on parameter;
public annotation v7 on return;
annotation Annot v8 on service, class;

public const annotation map<string> v9 on source listener;
const annotation map<string> v10 on source annotation;
const annotation map<int> v11 on source var;
public const annotation map<string> v12 on source const;
const annotation map<string> v13 on source external;
const annotation map<boolean> v15 on source worker;

@v1 {
    val: "v1 value"
}
public type T1 record {
    string name;
};

@v1 {
    val: "v1 value object"
}
@v2 {
    val: "v2 value"
}
class T2 {
    string name = "ballerina";

    @v3 {
        val: "v31 value"
    }
    @v4 {
        val: 41
    }
    public function setName(@v6 { val: "v61 value required" } string name,
                            @v6 { val: "v61 value defaultable" } int id = 0,
                            @v6 { val: "v61 value rest" } string... others) returns @v7 () {
        self.name = name;
    }
}

@v3 {
    val: "v33 value"
}
public function func(@v6 { val: "v63 value required" } int id,
                     @v6 { val: "v63 value defaultable" } string s = "hello",
                     @v6 { val: "v63 value rest" } float... others) returns @v7 float {
    return 1.0;
}

@v10 {
    str: "v10 value"
}
const annotation map<string> v14 on source annotation;

@v11 {
    val: 11
}
int i = 12;
@v11 {
    val: 2
}
[int, string] [intVar, stringVar] = [1, "myString"];

type myRecord record {int a;};
@v11 {
    val: 3
}
myRecord {a:myA} = {a:5};

type myError error<myRecord>;

@v11 {
    val: 4
}
myError error(message, a = errorNo) = error myError("error message", a = 5);

@v12 {
    str: "v12 value"
}
const F = 123.4;

@v9 {
    val: "v91"
}
listener Listener lis = new;

@v8 {
    val: "v8"
}
service /ser on lis {

    @v3 {
        val: "v34"
    }
    @v5 {
        val: "54"
    }
    resource function get res(@v6 { val: "v64" } int intVal) returns @v7 error? {
        return;
    }
}

service object {} serTwo = @v8 {
                 val: "v82"
              } service object {

    @v5 {
        val: "542"
    }
    resource function get res(@v6 { val: "v642" } int intVal) returns @v7 () {
        return;
    }
};

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

function externalFunction(boolean b) returns @v7 string =
    @v13 { strOne: "one", strTwo: "two" }
    @java:Method { 'class: "org.ballerinalang.nativeimpl.jvm.tests.Annotations" } external;

// Test compilation for annotations with the worker attach point.
function funcWithWorker() {

    @v15 {
        val: true
    }
    worker w1 {
        // do nothing
    }
}

function funcWithFuture() {
    future<()> fn =
    @v15 {
        val: false
    }
    start funcWithWorker();
}

function myFunctionWithWorkers() {
    @strand
    worker w {

    }
}

type A record {|
    string val = "ABC";
|};

public annotation A v16 on type;
public annotation A v17 on function;
public annotation A v18 on object function;
annotation A v19 on parameter;
annotation A v20 on service, class;

@v16
type MyType int|string;

@v17
function myFunction1(@v19 string name) returns string {
    return "Hello " + name;
}

service object {} serviceThree = @v20 service object {
    @v18
    resource function get res(@v19 int intVal) {
    }
};

public annotation map<int> v21 on function;
annotation map<int> v22 on parameter;

@v21
function myFunction2(@v22 string name) returns string {
    return "Hello " + name;
}

type B map<int>;
annotation B[] v23 on function;

@v23
@v23
public function myFunction3(string... argv) {
}

@v17 {}
function myFunction4(@v19 {} string name) returns string {
    return "Hello " + name;
}

@v23 {}
@v23 {}
public function myFunction5(string... argv) {
}

type C record { int i?; };

annotation C[] v24 on function;

@v24
@v24
public function myFunction6(string... argv) {
}

type Foo2 record {
    string s1;
    string? s2;
};

annotation Foo2 f2 on function;

@f2 {
    s1: "str",
    s2: null
}
public function fooFunction() {
}

service /introspection on lis {
    resource function get res(@v6 { val: "v64" } int intVal) returns @v7 error? {
        return;
    }
}

public const annotation A v25 on type;
public const annotation map<int> v26 on type;

@v25
@v26
type MyType2 int[]|string;

public annotation v27 on type;
public const annotation v28 on source const;

@v27
public enum Color1 {
    RED,
    BLUE
}

@v27
public enum Color2 {
    @v28
    RED,
    BLUE
}

annotation v30 on field;

type Tp [@v30 int, string];

public const annotation record {| int increment = 1; |} v29 on source type;

@v29 {
    increment: -1
}
type Qux record {|
    int x;
|};
