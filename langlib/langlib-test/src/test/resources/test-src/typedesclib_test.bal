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

type Person record {
    string name;
    int age;
};

function testRecToJson() returns json|error {
    Person  p = {name: "N", age: 3};
    json|error j = typedesc<json>.constructFrom(p);
    return j;
}

function testOnTypeName() returns [Person|error, json|error] {
   json p = { name : "tom", age: 2};
   Person|error pp = Person.constructFrom(p);

   Person s = { name : "bob", age: 4};
   json|error ss = json.constructFrom(s);

   return [pp, ss];
}

type BRec record {
    int i;
};

type CRec record {
    int i?;
};

public function testOptionalFieldToMandotoryField() returns any|error {
    CRec c = {  };
    var b = BRec.constructFrom(c);
    return b;
}

type Foo record {
    string s;
};

type Bar record {|
    float f?;
    string s;
|};

type Baz record {|
    int i?;
    string s;
|};

function testAmbiguousTargetType() returns boolean {
    Foo f = { s: "test string" };
    Bar|Baz|error bbe = (Bar|Baz).constructFrom(f);
    return bbe is error && bbe.reason() == "{ballerina/lang.typedesc}ConversionError" &&
            bbe.detail()?.message == "'Foo' value cannot be converted to 'Bar|Baz': ambiguous target type";
}

function testConstructFromForNilPositive() returns boolean {
    anydata a = ();
    string|error? c1 = (string?).constructFrom(a);
    json|error c2 = json.constructFrom(a);
    return c1 is () && c2 is ();
}

function testConstructFromForNilNegative() returns boolean {
    anydata a = ();
    string|int|error c1 = (string|int).constructFrom(a);
    Foo|Bar|error c2 = (Foo|Bar).constructFrom(a);
    return c1 is error && c2 is error && c1.detail()?.message == "cannot convert '()' to type 'string|int'";
}

function testConstructFromWithNumericConversion1() returns boolean {
    int a = 1234;
    float|error b = float.constructFrom(a);
    return b is float && b == 1234.0;
}

function testConstructFromWithNumericConversion2() returns boolean {
    anydata a = 1234.6;
    int|error b = int.constructFrom(a);
    return b is int && b == 1235;
}

type X record {
    int a;
    string b;
    float c;
};

type Y record {|
    float a;
    string b;
    decimal...;
|};

function testConstructFromWithNumericConversion3() returns boolean {
    X x = { a: 21, b: "Alice", c : 1000.5 };
    Y|error y = Y.constructFrom(x);
    return y is Y && y.a == 21.0 && y.b == "Alice" && y["c"] == <decimal> 1000.5;
}

function testConstructFromWithNumericConversion4() returns boolean {
    json j = { a: 21.3, b: "Alice", c : 1000 };
    X|error x = X.constructFrom(j);
    return x is X && x.a == 21 && x.b == "Alice" && x.c == 1000.0;
}

function testConstructFromWithNumericConversion5() returns boolean {
    int[] i = [1, 2];
    float[]|error j = float[].constructFrom(i);
    (float|boolean)[]|error j2 = (float|boolean)[].constructFrom(i);
    return j is float[] && j.length() == i.length() && j[0] == 1.0 && j[1] == 2.0 && j == j2;
}

function testConstructFromWithNumericConversion6() returns boolean {
    map<float> m = { a: 1.2, b: 2.7 };
    map<int>|error m2 = map<int>.constructFrom(m);
    map<string|int>|error m3 = map<string|int>.constructFrom(m);
    return m2 is map<int> && m.length() == m2.length() && m2["a"] == 1 && m2["b"] == 3 && m2 == m3;
}

function testConstructFromWithNumericConversion7() returns boolean {
    int[] a1 = [1, 2, 3];
    decimal[]|error a2 = decimal[].constructFrom(a1);
    return a2 is decimal[] && a2.length() == a1.length() && a2[0] == <decimal> 1 && a2[1] == <decimal> 2 &&
        a2[2] == <decimal> 3;
}

function testConstructFromSuccessWithMoreThanOneNumericTarget() returns boolean {
    map<int> m1 = { a: 1, b: 2 };
    // Successful since value conversion is not required.
    map<float|int|decimal>|error m2 = map<float|int|decimal>.constructFrom(m1);
    return m2 is map<float|int|decimal> && m1 == m2;
}

function testConstructFromFailureWithAmbiguousNumericConversionTarget() returns boolean {
    int[] i = [1, 2];
    (float|decimal|boolean)[]|error j = (float|decimal|boolean)[].constructFrom(i); // two possible conversion types
    return j is error && j.reason() == "{ballerina/lang.typedesc}ConversionError" &&
            j.detail()?.message == "'int[]' value cannot be converted to 'float|decimal|boolean[]'";
}

type A record {
    int i = 4;
    string s;
    B b;
    float f = 2.0;
};

type B record {
    string p;
    string q = "world";
};

type C record {
    string s;
    map<string> b;
    anydata f;
};

function testSettingRecordDefaultValuesOnConversion() returns boolean {
    C c = { f: 34, s: "test", b: { p: "hello" } };
    A|error d = A.constructFrom(c);
    return d is A && d.i == 4 && d.s == "test" && d.b.p == "hello" && d.b.q == "world" && d.f == 34.0;
}

type D record {
    int i;
};

type E record {
    string s;
};

type F record {
    float f;
};

function testConvertingToUnionConstrainedType() returns boolean {
    map<D> f = {x:{i:1}};
    json j = <json> json.constructFrom(f);
    return map<D|E|F>.constructFrom(j) == f;
}
