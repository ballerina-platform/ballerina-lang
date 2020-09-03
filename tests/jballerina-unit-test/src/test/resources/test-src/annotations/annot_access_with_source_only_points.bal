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

public const annotation map<string> v1 on source type, class;

const STRING_VAL = "string value";
const STRING_VAL_TWO = "string value two";

@v1 {
    foo: STRING_VAL,
    bar: "1"
}
public type T1 record {
    string name;
};

@v1 {
    foo: STRING_VAL_TWO
}
class T2 {
    string name = "ballerina";
}

function testAnnotAccessForAnnotWithSourceOnlyPoints1() returns boolean {
    T1 a = { name: "ballerina" };
    typedesc<any> t = typeof a;
    map<string>? annot = t.@v1; // Should be`()` at runtime since it matches `source type`
    return annot is ();
}

function testAnnotAccessForAnnotWithSourceOnlyPoints2() returns boolean {
    T2 a = new;
    typedesc<any> t = typeof a;
    map<string>? annot = t.@v1; // Should not be `()` at runtime since it matches `object type`
    return annot is map<string> && annot["foo"] == STRING_VAL_TWO;
}

const annotation map<string> v2 on type, source class;

@v2 {
    foo: STRING_VAL,
    bar: "1"
}
public type T3 record {
    string name;
};

@v2 {
    foo: STRING_VAL_TWO
}
class T4 {
    string name = "ballerina";
}

function testAnnotAccessForAnnotWithSourceOnlyPoints3() returns boolean {
    T3 a = { name: "ballerina" };
    typedesc<any> t = typeof a;
    map<string>? annot = t.@v2; // Should not be `()` at runtime since it matches `type`
    return annot is map<string> && annot["foo"] == STRING_VAL && annot["bar"] == "1";
}

function testAnnotAccessForAnnotWithSourceOnlyPoints4() returns boolean {
    T4 a = new;
    typedesc<any> t = typeof a;
    map<string>? annot = t.@v2; // Should be`()` at runtime since it matches `source object type`
    return annot is ();
}

public const annotation v3 on source type, source class;

@v3
public type T5 record {
    string name;
};

@v3
class T6 {
    string name = "ballerina";
}

function testAnnotAccessForAnnotWithSourceOnlyPoints5() returns boolean {
    T5 a = { name: "ballerina" };
    typedesc<any> t = typeof a;
    boolean? annot = t.@v3; // Should be`()` at runtime since it matches `source type`
    return annot is ();
}

function testAnnotAccessForAnnotWithSourceOnlyPoints6() returns boolean {
    T6 a = new;
    typedesc<any> t = typeof a;
    return t.@v3 is (); // Should be`()` at runtime since it matches `source type`
}
