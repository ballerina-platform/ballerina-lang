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

import annots/defn;

public const annotation Allow on parameter, source var;

public const annotation Custom on source annotation;

public const annotation Ann on type;

public const annotation Member on field;

@Ann
public type Recx record {|
    @Member int x1;
    string y1;
|};

@Ann
public type Tup [@Member int, string];

public type T1 [int, @Member int, string...];
public type T2 [int, @Member int, string];

@Custom
public annotation map<int> NonConstAllow on parameter;

public function func(@Allow int i) {
}

int iVal = 12345;

@Allow @defn:Annot {i: 321} int jVal = 6543;

@defn:KnownConst
const C = 1;

const int D = C;

public function otherFunc(int i, @defn:Annot {i: 456} @defn:NonConstAnnot {i: iVal} int j = 1, @Allow int... k) {
}

public function anotherFunc(@NonConstAllow {x: 1, y: iVal} int i) {
}

public class TestListener {
    public function init(string token, @defn:Expose @Allow int? listenOn = 8090) {
    }

    public function attach(@defn:Annot {i: 1} service object {} s, string[]|string? name) returns error? {
    }

    public function detach(@defn:Annots {i: 2} @defn:Annots {i: 3} service object {} s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public isolated function immediateStop() returns error? {
    }
}

public type Foo record {|
    string s;
    string t;
    boolean b = false;
|};

public type Bar record {|
    Foo[] f1;
    [Foo, string] f2;
    int i = 10;
|};

public const annotation Bar ClassAnnot on source class;

const s2 = "s2";
const t3 = "t3";

@ClassAnnot {
    f1: [
        { s: "s", t: "t" },
        { s: s2, t: "t2" }
    ],
    f2: [{ s: "s3", t: t3 }, "test"]
}
class Cl {

}

public const annotation FunctionAnnot on function;
public const annotation ReturnAnnot on return;

@FunctionAnnot
public function fn1() returns @ReturnAnnot int {
    return 0;
}

public function fn2() {

}

public class Cl2 {
    public function cfn1() returns @ReturnAnnot string {
        return "";
    }

    @FunctionAnnot
    public function cfn2() {

    }
}

const THREE = 3;

public const annotation record {| int[] arr; |} AnnotWithList on type;

@AnnotWithList {
    arr: [1, 2, THREE]
}
public type TypeWithListInAnnots record {|
    int a;
    int b;
|};
