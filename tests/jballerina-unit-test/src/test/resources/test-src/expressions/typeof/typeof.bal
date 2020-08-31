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


type RecType0 record {
    string name;
};

function typeDescOfARecord() returns typedesc<any> {
    RecType0 i = { name: "theName"};
    typedesc<any> td0 = typeof i;
    return td0;
}

class Obj0 {
    string a;
    int b;

    function init(string a, int b) {
        self.a = a;
        self.b = b;
    }
}

function typeDescOrAObject() returns typedesc<any> {
    Obj0 o = new("name", 42);
    return typeof o;
}

function typeDescOfLiterals() returns
    [typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>] {
    var a = typeof 1;
    var b = typeof 2.0;
    var c = typeof 2.1f;
    var d = typeof "str-literal";
    var e = typeof true;
    var f = typeof false;
    var g = typeof ();
    return [a, b, c, d, e, f, g];
}

function typeDescOfExpressionsOfLiterals() returns
    [typedesc<any>, typedesc<any>] {
    int i = 0;
    int j = 4;
    int k = 4;
    float f = 0.0;
    float ff = 22.0;
    return [typeof (i+j*k), typeof (f*ff)];
}

function takesATypedescParam(typedesc<any> param) returns typedesc<any> {
    return param;
}

function passTypeofToAFunction() returns typedesc<any> {
    typedesc<any> t = typeof 22;
    var t1 = takesATypedescParam(t);
    var t2 = takesATypedescParam(typeof 33);
    return t1;
}

function takeTypeofAsRestParams(typedesc<any>... xs) returns typedesc<any>[] {
    return xs;
}

function passTypeofAsRestParams() returns typedesc<any>[] {
    return takeTypeofAsRestParams(typeof 22, typeof 33, typeof 33.33);
}

function returnTypeOfInt() returns typedesc<any> {
    return typeof (5 + 1);
}
