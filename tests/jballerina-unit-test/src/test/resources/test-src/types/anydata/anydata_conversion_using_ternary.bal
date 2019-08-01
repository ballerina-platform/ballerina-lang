// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Foo record {
    int a;
};

type ClosedFoo record {|
    int ca;
|};

type Employee record {|
    int id;
    string name;
    float salary;
|};

type ValueType int|float|string|boolean|byte;
type DataType ValueType|table<any>|json|xml|ClosedFoo|Foo|map<anydata>|anydata[]|();

function testAnydataToValueTypes() returns [int, float, boolean, string] {
    anydata ad = 33;
    int i = ad is int ? ad : -1;

    ad = 23.45;
    float f = ad is float ? ad : -1.0;

    ad = true;
    boolean bool = ad is boolean ? ad : false;

    ad = "Hello World!";
    string s = ad is string ? ad : "";

    return [i, f, bool, s];
}

function testAnydataToJson() returns json {
    json j = { name: "apple", color: "red", price: 40 };
    anydata adJ = j;
    json convertedJ = adJ is json ? adJ : ();
    return convertedJ;
}

function testAnydataToXml() returns xml {
    xml x = xml `<book>The Lost World</book>`;
    anydata adx = x;
    xml convertedX = adx is xml ? adx : xml ` `;
    return convertedX;
}

function testAnydataToRecord() returns [Foo, ClosedFoo] {
    Foo foo = {a: 15};
    anydata adr = foo;
    Foo convertedFoo = adr is Foo ? adr : {a: -1};

    ClosedFoo cFoo = {ca: 20};
    adr = cFoo;
    ClosedFoo convertedCFoo = adr is ClosedFoo ? adr : {ca: -1};

    return [convertedFoo, convertedCFoo];
}

function testAnydataToUnion() returns ValueType?[] {
    anydata ad = 10;
    ValueType?[] vt = [];
    int i = 0;

    vt[i] = ad is ValueType ? ad : -1;
    i += 1;

    ad = 23.45;
    vt[i] = ad is ValueType ? ad : -1.0;
    i += 1;

    ad = "hello world!";
    vt[i] = ad is ValueType ? ad : "";
    i += 1;

    ad = true;
    vt[i] = ad is ValueType ? ad : false;
    i += 1;

    byte b = 255;
    ad = b;
    vt[i] = ad is ValueType ? ad : 0;
    i += 1;

    return vt;
}

function testAnydataToUnion2() returns DataType[] {
    anydata ad;
    DataType[] dt = [];
    int i = 0;

    json j = { name: "apple", color: "red", price: 40 };
    ad = j;
    dt[i] = ad is DataType ? ad : ();
    i += 1;

    xml x = xml `<book>The Lost World</book>`;
    ad = x;
    dt[i] = ad is DataType ? ad : xml ` `;
    i += 1;

    Foo foo = {a: 15};
    Foo invFoo = {a: -1};
    ad = foo;
    dt[i] = ad is DataType ? ad : invFoo;
    i += 1;

    ClosedFoo cfoo = {ca: 15};
    ClosedFoo invCFoo = {ca: -1};
    ad = cfoo;
    dt[i] = ad is DataType ? ad : invCFoo;
    i += 1;

    return dt;
}

function testAnydataToTuple() returns [int, float, boolean, string, byte]? {
    anydata ad;

    byte b = 255;
    [int, float, boolean, string, byte] vt = [123, 23.45, true, "hello world!", b];
    ad = vt;

    return ad is [int, float, boolean, string, byte] ? ad : ();
}

function testAnydataToTuple2() returns [json, xml]? {
    anydata ad;

    json j = { name: "apple", color: "red", price: 40 };
    xml x = xml `<book>The Lost World</book>`;
    [json, xml] jxt = [j, x];
    ad = jxt;

    return ad is [json, xml] ? ad : ();
}

function testAnydataToTuple3() returns [[DataType[], string], int, float]? {
    anydata ad;

    json j = { name: "apple", color: "red", price: 40 };
    xml x = xml `<book>The Lost World</book>`;
    DataType[] dt = [j, x];
    [DataType[], string] ct = [dt, "hello world!"];
    [[DataType[], string], int, float] nt = [ct, 123, 23.45];
    ad = nt;

    return ad is [[DataType[], string], int, float] ? ad : ();
}

function testAnydataToNil() returns int? {
    () nil = ();
    anydata ad = nil;
    return ad is () ? ad : -1;
}

function testTypeCheckingOnAny() returns anydata {
    any a = 10;
    anydata[] ad = [];
    int i = 0;

    ad[i] = a is anydata ? a : ();
    i += 1;

    a = 23.45;
    ad[i] = typeCheck(a);
    i += 1;

    a = true;
    ad[i] = typeCheck(a);
    i += 1;

    a = "hello world!";
    ad[i] = typeCheck(a);
    i += 1;

    json j = { name: "apple", color: "red", price: 40 };
    a = j;
    ad[i] = a is anydata ? a : ();
    i += 1;

    a = xml `<book>The Lost World</book>`;
    ad[i] = typeCheck(a);
    i += 1;

    Foo foo = {a: 15};
    a = foo;
    ad[i] = typeCheck(a);
    i += 1;

    ClosedFoo cfoo = {ca: 15};
    a = cfoo;
    ad[i] = typeCheck(a);
    i += 1;

    return ad;
}

function typeCheck(any a) returns anydata {
    return a is anydata ? a : ();
}
