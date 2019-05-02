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

function typeDescOfARecord() returns typedesc {
    RecType0 i = { name: "theName"};
    typedesc td0 = typeof i;
    return td0;
}

type Obj0 object {
    string a;
    int b;

    function __init(string a, int b) {
        self.a = a;
        self.b = b;
    }
};

function typeDescOrAObject() returns typedesc {
    Obj0 o = new("name", 42);
    return typeof o;
}

function typeDescOfLiterals() returns
    (typedesc, typedesc, typedesc, typedesc, typedesc, typedesc, typedesc) {
    var a = typeof 1;
    var b = typeof 2.0;
    var c = typeof 2.1f;
    var d = typeof "str-literal";
    var e = typeof true;
    var f = typeof false;
    var g = typeof ();
    return (a, b, c, d, e, f, g);
}

function typeDescOfExpressionsOfLiterals() returns
    (typedesc, typedesc) {
    int i = 0;
    int j = 4;
    int k = 4;
    float f = 0.0;
    float ff = 22.0;
    return (typeof (i+j*k), typeof (f*ff));
}

function takesATypedescParam(typedesc param) returns typedesc {
    return param;
}

function passTypeofToAFunction() returns typedesc {
    typedesc t = typeof 22;
    var t1 = takesATypedescParam(t);
    var t2 = takesATypedescParam(typeof 33);
    return t1;
}

function takeTypeofAsRestParams(typedesc... xs) returns typedesc[] {
    return xs;
}

function passTypeofAsRestParams() returns typedesc[] {
    return takeTypeofAsRestParams(typeof 22, typeof 33, typeof 33.33);
}

function returnTypeOfInt() returns typedesc {
    return typeof (5 + 1);
}
