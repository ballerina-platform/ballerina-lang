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
    int a = 0;
};

type ClosedFoo record {|
    int ca = 0;
|};

type Employee record {|
    readonly int id;
    string name;
    float salary;
|};
    
type Person record {|
    string name = "";
    int age = 0;
    Person? parent = ();
    json info = ();
    map<anydata>? address = ();
    int[]? marks = ();
    anydata a = ();
    float score = 0.0;
    boolean alive = false;
    Person[]? children = ();
|};

function testLiteralValueAssignment() returns [anydata, anydata, anydata, anydata] {
    anydata adi = 10;
    anydata adf = 23.45;
    anydata adb = true;
    anydata ads = "Hello World!";
    return [adi, adf, adb, ads];
}

function testValueTypesAssignment() returns [anydata, anydata, anydata, anydata] {
    int i = 10;
    anydata adi = i;

    float f = 23.45;
    anydata adf = f;

    boolean b = true;
    anydata adb = b;

    string s = "Hello World!";
    anydata ads = s;

    return [adi, adf, adb, ads];
}

function testRecordAssignment() returns [anydata, anydata] {
    Foo f = {a: 20};
    anydata adr = f;

    ClosedFoo cf = {ca: 35};
    anydata adcr = cf;

    return [adr, adcr];
}

function testCyclicRecordAssignment() returns (anydata) {
    Person p = {name:"Child",
                age:25,
                parent:{name:"Parent", age:50},
                address:{"city":"Colombo", "country":"SriLanka"},
                info:{status:"single"},
                marks:[67, 38, 91]
    };
    anydata adp = p;
    any p2 =  <Person> adp;
    if(p2 is anydata){
        return p2;
    } else {
        return ();
    }
}

function testXMLAssignment() returns [anydata, anydata] {
    anydata adxl = xml `<book>The Lost World</book>`;

    xml x = xml `<book>Count of Monte Cristo</book>`;
    anydata adx = x;

    return [adxl, adx];
}

function testJSONAssignment() returns anydata {
    json j = {name: "apple", color: "red", price: 40};
    anydata adj = j;
    return adj;
}

function testTableAssignment() {
    table<Employee> t = table key(id) [
          { id: 1, name: "Mary", salary: 300.5 },
          { id: 2, name: "John", salary: 200.5 },
          { id: 3, name: "Jim", salary: 330.5 }
        ];

    anydata adt = t;
    string employeeListAsString = "[{\"id\":1,\"name\":\"Mary\",\"salary\":300.5},{\"id\":2,\"name\":\"John\",\"salary\":200.5},{\"id\":3,\"name\":\"Jim\",\"salary\":330.5}]";
    assertEquality(employeeListAsString, adt.toString());
}

function testMapAssignment() {
    anydata ad;

    map<int> mi = {};
    ad = mi;

    map<float> mf = {};
    ad = mf;

    map<boolean> mb = {};
    ad = mb;

    map<byte> mby = {};
    ad = mby;

    map<string> ms = {};
    ad = ms;

    map<json> mj = {};
    ad = mj;

    map<xml> mx = {};
    ad = mx;

    map<Foo> mr = {};
    ad = mr;

    map<ClosedFoo> mcr = {};
    ad = mcr;

    map<table<map<any>>> mt = {};
    ad = mt;

    map<map<anydata>> mmad = {};
    ad = mmad;

    map<anydata[]> madr = {};
    ad = madr;

    map<DataType> mu = {};
    ad = mu;

    map<[[DataType, string], int, float]> mtup = {};
    ad = mtup;

    map<()> mnil = {};
    ad = mnil;
}

function testConstrainedMaps(){
    byte b = 10;
    Foo foo = {a: 15};
    json j = {name: "apple", color: "red", price: 40};
    xml x = xml `<book>The Lost World</book>`;
    map<string> smap = {};

    smap["foo"] = "foo";
    smap["bar"] = "bar";

    map<anydata> adm = {};
    adm["byte"] = b;
    adm["json"] = j;
    adm["record"] = foo;
    adm["xml"] = x;
    adm["string"] = "Hello World";
    adm["int"] = 1234;
    adm["float"] = 23.45;
    adm["boolean"] = true;
    adm["map"] = smap;
    adm["nil"] = ();

    table<Employee> t = table key(id) [
        { id: 1, name: "Mary", salary: 300.5 },
        { id: 2, name: "John", salary: 200.5 },
        { id: 3, name: "Jim", salary: 330.5 }
    ];
    adm["table"] = t;

    assertEquality(1234, adm["int"]);
    assertEquality(23.45, adm["float"]);
    assertEquality(true, adm["boolean"]);
    assertEquality("Hello World", adm["string"]);
    assertEquality("10", adm["byte"].toString());
    assertEquality("<book>The Lost World</book>", adm["xml"].toString());
    assertEquality(foo, adm["record"]);
    assertEquality(smap, adm["map"]);
    assertEquality(j, adm["json"]);
    string employeeListAsString = "[{\"id\":1,\"name\":\"Mary\",\"salary\":300.5},{\"id\":2,\"name\":\"John\",\"salary\":200.5},{\"id\":3,\"name\":\"Jim\",\"salary\":330.5}]";
    assertEquality(employeeListAsString, adm["table"].toString());
}

function testArrayAssignment() {
    anydata ad;

    int[] ai = [10, 20, 30];
    ad = ai;

    float[] af = [1.2, 2.3, 3.4];
    ad = af;

    boolean[] ab = [true, false, true];
    ad = ab;

    byte[] aby = [1, 2, 3];
    ad = aby;

    string[] ast = ["foo", "bar"];
    ad = ast;

    json[] aj = ["foo", "bar"];
    ad = aj;

    xml[] ax = [xml `<book>The Lost World</book>`];
    ad = ax;

    Foo[] ar = [{a:10}, {a:20}];
    ad = ar;

    ClosedFoo[] acr = [{ca:10}, {ca:20}];
    ad = acr;

    table<Employee> t = table key(id) [
            { id: 1, name: "Mary", salary: 300.5 },
            { id: 2, name: "John", salary: 200.5 },
            { id: 3, name: "Jim", salary: 330.5 }
        ];
    table<Employee>[] at = [t];
    ad = at;

    map<anydata> m = {};
    map<anydata>[] amad = [m];
    ad = amad;

    anydata[] aad = [];
    ad = aad;

    anydata[][] a2ad = [];
    ad = a2ad;

    ()[] an = [];
    ad = an;
}

function testAnydataArray() returns anydata[] {
    Foo foo = {a:15};
    json j = {name: "apple", color: "red", price: 40};
    xml x = xml `<book>The Lost World</book>`;
    byte b = 10;
    anydata[] ad = [1234, 23.45, true, "Hello World!", b, foo, j, x];
    return ad;
}

type ValueType int|float|string|boolean|byte;
type DataType ValueType|table<map<any>>|json|xml|ClosedFoo|Foo|map<anydata>|anydata[]|();

function testUnionAssignment() returns anydata[] {
    anydata[] rets = [];
    int i = 0;

    ValueType vt = "hello world!";
    rets[i] = vt;
    i += 1;

    vt = 123;
    rets[i] = vt;
    i += 1;

    vt = 23.45;
    rets[i] = vt;
    i += 1;

    vt = true;
    rets[i] = vt;
    i += 1;

    byte b = 255;
    vt = b;
    rets[i] = vt;
    i += 1;

    return rets;
}

function testUnionAssignment2(){
    anydata[] rets = [];
    int i = 0;

    DataType dt = "hello world!";
    rets[i] = dt;
    i += 1;

    table<Employee> t = table key(id) [
            { id: 1, name: "Mary", salary: 300.5 },
            { id: 2, name: "John", salary: 200.5 },
            { id: 3, name: "Jim", salary: 330.5 }
        ];
    dt = t;
    rets[i] = dt;
    i += 1;

    json j = {name: "apple", color: "red", price: 40};
    dt = j;
    rets[i] = dt;
    i += 1;

    dt = xml `<book>The Lost World</book>`;
    rets[i] = dt;
    i += 1;

    Foo foo = {a:15};
    dt = foo;
    rets[i] = dt;
    i += 1;

    ClosedFoo cfoo = {ca:15};
    dt = cfoo;
    rets[i] = dt;
    i += 1;

    map<anydata> m = {};
    m["foo"] = foo;
    dt = m;
    rets[i] = dt;
    i += 1;

    anydata[] adr = [];
    adr[0] = "hello world!";
    dt = adr;
    rets[i] = dt;
    i += 1;

    assertEquality("hello world!", rets[0].toString());
    string employeeListAsString = "[{\"id\":1,\"name\":\"Mary\",\"salary\":300.5},{\"id\":2,\"name\":\"John\",\"salary\":200.5},{\"id\":3,\"name\":\"Jim\",\"salary\":330.5}]";
    assertEquality(employeeListAsString, rets[1].toString());
    assertEquality("{\"name\":\"apple\",\"color\":\"red\",\"price\":40}", rets[2].toString());
    assertEquality("<book>The Lost World</book>", rets[3].toString());
    assertEquality(foo, rets[4]);
    assertEquality(cfoo, rets[5]);
    assertEquality(m, rets[6]);
}

function testTupleAssignment(){
    anydata[] rets = [];
    int i = 0;

    byte b = 255;
    [int, float, boolean, string, byte] vt = [123, 23.45, true, "hello world!", b];
    rets[i] = vt;
    i += 1;

    json j = { name: "apple", color: "red", price: 40 };
    xml x = xml `<book>The Lost World</book>`;
    [json, xml] jxt = [j, x];
    rets[i] = jxt;
    i += 1;

    DataType[] dt = [j, x];
    [DataType[], string] ct = [dt, "hello world!"];
    rets[i] = ct;
    i += 1;

    [[DataType[], string], int, float] nt = [ct, 123, 23.45];
    rets[i] = nt;

    assertEquality("123 23.45 true hello world! 255", rets[0].toString());
    assertEquality(jxt, rets[1]);
    assertEquality(ct, rets[2]);
    assertEquality(nt, rets[3]);
}

function testNilAssignment() returns anydata {
    anydata ad = ();
    return ad;
}

type FiniteT "A"|"Z"|123|23.45|true;

function testFiniteTypeAssignment() returns anydata {
    anydata[] ad = [];
    int i = 0;

    FiniteT ft = "A";
    ad[i] = ft;
    i += 1;

    ft = "Z";
    ad[i] = ft;
    i += 1;

    ft = 123;
    ad[i] = ft;
    i += 1;

    ft = 23.45;
    ad[i] = ft;
    i += 1;

    ft = true;
    ad[i] = ft;
    i += 1;

    return ad;
}

function testAnydataToValueTypes() returns [int, float, boolean, string] {
    anydata ad = 33;
    int i = 0;
    if (ad is int) {
        i = ad;
    }

    ad = 23.45;
    float f = 0;
    if (ad is float) {
        f = ad;
    }

    ad = true;
    boolean bool = false;
    if (ad is boolean) {
        bool = ad;
    }

    ad = "Hello World!";
    string s = "";
    if (ad is string) {
        s = ad;
    }

    return [i, f, bool, s];
}

function testAnydataToJson() returns json {
    json j = { name: "apple", color: "red", price: 40 };
    anydata adJ = j;
    json convertedJ = null;
    if (adJ is json) {
        convertedJ = adJ;
    }
    return convertedJ;
}

function testAnydataToXml() returns xml {
    xml x = xml `<book>The Lost World</book>`;
    anydata adx = x;
    xml convertedX = xml ` `;
    if (adx is xml) {
        convertedX = adx;
    }
    return convertedX;
}

function testAnydataToRecord() returns [Foo, ClosedFoo] {
    Foo foo = {a: 15};
    anydata adr = foo;
    Foo convertedFoo = {};
    if (adr is Foo) {
        convertedFoo = adr;
    }

    ClosedFoo cFoo = {ca: 20};
    adr = cFoo;
    ClosedFoo convertedCFoo = {};
    if (adr is ClosedFoo) {
        convertedCFoo = adr;
    }
    return [convertedFoo, convertedCFoo];
}

function testAnydataToMap() {
    anydata ad;

    map<int> mi = {};
    ad = mi;
    map<int> convertedMi;
    if (ad is map<int>) {
        convertedMi = ad;
    }

    map<float> mf = {};
    ad = mf;
    map<float> convertedMf;
    if (ad is map<float>) {
        convertedMf = ad;
    }

    map<boolean> mb = {};
    ad = mb;
    map<boolean> convertedMb;
    if (ad is map<boolean>) {
        convertedMb = ad;
    }

    map<byte> mby = {};
    ad = mby;
    map<byte> convertedMby;
    if (ad is map<byte>) {
        convertedMby = ad;
    }

    map<string> ms = {};
    ad = ms;
    map<string> convertedMs;
    if (ad is map<string>) {
        convertedMs = ad;
    }

    map<json> mj = {};
    ad = mj;
    map<json> convertedMj;
    if (ad is map<json>) {
        convertedMj = ad;
    }

    map<xml> mx = {};
    ad = mx;
    map<xml> convertedMx;
    if (ad is map<xml>) {
        convertedMx = ad;
    }

    map<Foo> mr = {};
    ad = mr;
    map<Foo> convertedMfoo;
    if (ad is map<Foo>) {
        convertedMfoo = ad;
    }

    map<ClosedFoo> mcr = {};
    ad = mcr;
    map<ClosedFoo> convertedMCfoo;
    if (ad is map<ClosedFoo>) {
        convertedMCfoo = ad;
    }

    map<table<map<any>>> mt = {};
    ad = mt;
    map<table<map<any>>> convertedMt;
    if (ad is map<table<map<any>>>) {
        convertedMt = ad;
    }

    map<map<anydata>> mmad = {};
    ad = mmad;
    map<map<anydata>> convertedMmad;
    if (ad is map<map<anydata>>) {
        convertedMmad = ad;
    }

    map<anydata[]> madr = {};
    ad = madr;
    map<anydata[]> convertedMadr;
    if (ad is map<anydata[]>) {
        convertedMadr = ad;
    }

    map<DataType> mu = {};
    ad = mu;
    map<DataType> convertedMu;
    if (ad is map<DataType>) {
        convertedMu = ad;
    }

    map<[[DataType, string], int, float]> mtup = {};
    ad = mtup;
    map<[[DataType, string], int, float]> convertedMtup;
    if (ad is map<[[DataType, string], int, float]>) {
        convertedMtup = ad;
    }

    map<()> mnil = {};
    ad = mnil;
    map<()> convertedMnil;
    if (ad is map<()>) {
        convertedMnil = ad;
    }
}

function testAnydataToTable(){
    table<Employee> t = table key(id)[
                    { id: 1, name: "Mary", salary: 300.5 },
                    { id: 2, name: "John", salary: 200.5 },
                    { id: 3, name: "Jim", salary: 330.5 }
        ];

    anydata ad = t;
    table<Employee>|() convertedT = ();
    if (ad is table<Employee>) {
        convertedT = ad;
        assertEquality(t, convertedT);
    }
}

function testAnydataToUnion() returns ValueType?[] {
    anydata ad = 10;
    ValueType?[] vt = [];
    int i = 0;

    if (ad is ValueType) {
        vt[i] = ad;
        i += 1;
    }

    ad = 23.45;
    if (ad is ValueType) {
        vt[i] = ad;
        i += 1;
    }

    ad = "hello world!";
    if (ad is ValueType) {
        vt[i] = ad;
        i += 1;
    }

    ad = true;
    if (ad is ValueType) {
        vt[i] = ad;
        i += 1;
    }

    byte b = 255;
    ad = b;
    if (ad is ValueType) {
        vt[i] = ad;
        i += 1;
    }

    return vt;
}

function testAnydataToUnion2(){
    anydata ad;
    DataType[] dt = [];
    int i = 0;

    json j = { name: "apple", color: "red", price: 40 };
    ad = j;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    xml x = xml `<book>The Lost World</book>`;
    ad = x;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    table<Employee> t = table key(id) [
        { id: 1, name: "Mary", salary: 300.5 },
        { id: 2, name: "John", salary: 200.5 },
        { id: 3, name: "Jim", salary: 330.5 }
    ];

    ad = t;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    Foo foo = {a: 15};
    ad = foo;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    ClosedFoo cfoo = {ca: 15};
    ad = cfoo;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    map<anydata> m = {};
    m["foo"] = foo;
    ad = m;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    anydata[] adr = [];
    adr[0] = foo;
    ad = adr;
    if (ad is DataType) {
        dt[i] = ad;
        i += 1;
    }

    assertEquality(j, dt[0]);
    assertEquality(x, dt[1]);
    assertEquality(t, dt[2]);
    assertEquality(foo, dt[3]);
    assertEquality(cfoo, dt[4]);
    assertEquality(m, dt[5]);
    assertEquality(adr, dt[6]);
}

function testAnydataToTuple() returns [int, float, boolean, string, byte]? {
    anydata ad;

    byte b = 255;
    [int, float, boolean, string, byte] vt = [123, 23.45, true, "hello world!", b];
    ad = vt;
    if (ad is [int, float, boolean, string, byte]) {
        return ad;
    }

    return ();
}

function testAnydataToTuple2() returns [json, xml]? {
    anydata ad;

    json j = { name: "apple", color: "red", price: 40 };
    xml x = xml `<book>The Lost World</book>`;
    [json, xml] jxt = [j, x];
    ad = jxt;

    if (ad is [json, xml]) {
        return ad;
    }

    return ();
}

function testAnydataToTuple3() {
    anydata ad;

    json j = { name: "apple", color: "red", price: 40 };
    xml x = xml `<book>The Lost World</book>`;
    DataType[] dt = [j, x];
    [DataType[], string] ct = [dt, "hello world!"];
    [[DataType[], string], int, float] nt = [ct, 123, 23.45];
    ad = nt;

    if (ad is [[DataType[], string], int, float]) {
        assertEquality("[{\"name\":\"apple\",\"color\":\"red\",\"price\":40},`<book>The Lost World</book>`] hello world!", ad[0].toString());
    }
}

function testAnydataToNil() returns int? {
    () nil = ();
    anydata ad = nil;

    if (ad is ()) {
        return ad;
    }

    return -1;
}

function testAnydataToFiniteType() returns FiniteT?[] {
    FiniteT?[] ftar = [];
    int i = 0;

    FiniteT ft = "A";
    anydata ad = ft;
    if (ad is FiniteT) {
        ftar[i] = ad;
        i += 1;
    }

    ft = "Z";
    ad = ft;
    if (ad is FiniteT) {
        ftar[i] = ad;
        i += 1;
    }

    ft = 123;
    ad = ft;
    if (ad is FiniteT) {
        ftar[i] = ad;
        i += 1;
    }

    ft = 23.45;
    ad = ft;
    if (ad is FiniteT) {
        ftar[i] = ad;
        i += 1;
    }

    ft = true;
    ad = ft;
    if (ad is FiniteT) {
        ftar[i] = ad;
        i += 1;
    }

    return ftar;
}

function testTypeCheckingOnAny() returns anydata {
    any a = 10;
    anydata[] ad = [];
    int i = 0;

    if (a is anydata) {
        ad[i] = a;
        i += 1;
    }

    a = 23.45;
    if (a is anydata) {
        ad[i] = a;
        i += 1;
    }

    a = true;
    if (a is anydata) {
        ad[i] = a;
        i += 1;
    }

    a = "hello world!";
    if (a is anydata) {
        ad[i] = a;
        i += 1;
    }

    json j = { name: "apple", color: "red", price: 40 };
    a = j;
    if (a is anydata) {
        ad[i] = a;
        i += 1;
    }

    a = xml `<book>The Lost World</book>`;
    if (a is anydata) {
        ad[i] = a;
        i += 1;
    }

    Foo foo = {a: 15};
    a = foo;
    if (a is DataType) {
        ad[i] = a;
        i += 1;
    }

    ClosedFoo cfoo = {ca: 15};
    a = cfoo;
    if (a is DataType) {
        ad[i] = a;
        i += 1;
    }

    return ad;
}

type MyError error<MyErrorDetail>;

type MyErrorDetail record {|
    error e1;
    error e2;
    string message?;
    error cause?;
|};

error e1 = error("err reason");
error e2 = error("err reason 2", str = "string value", e1=e1);
MyError e3 = MyError("err reason 3", e1 = e1, e2 = e2);

function testArraysWithErrorsAsAnydata() returns boolean {
    error?[] a1 = [e1, e2];
    (anydata|error)[3] a2 = [e3, "hello", e1];

    anydata ad1 = a1;
    anydata ad2 = <anydata> a2;

    error?[] a3 = <error?[]> ad1;
    (anydata|error)[] a4 = <(anydata|error)[]> ad2;

    return a3[0] === e1 && a3[1] === e2 && a4[0] === e3 && a4[2] === e1 && a3 === a1 && a4 === a2;
}

function testTuplesWithErrorsAsAnydata() returns boolean {
    [error, MyError] a1 = [e1, e3];
    [anydata, error, string] a2 = [a1, e1, "hello"];

    anydata ad1 = <anydata> a1;
    anydata ad2 = a2;

    [error, MyError] a3 = <[error, MyError]> ad1;
    [anydata, error, string] a4 = <[anydata, error, string]> ad2;

    return a3[0] === e1 && a3[1] === e3 && a4[0] === a1 && a4[1] === e1 && a3 === a1 && a4 === a2;
}

function testMapsWithErrorsAsAnydata() returns boolean {
    map<error> a1 = { e1: e1, e3: e3 };
    map<string|error> a2 = { e1: e1, e2: e2, e3: e3 };

    anydata ad1 = a1;
    anydata ad2 = <anydata> a2;

    map<error> a3 = <map<error>> ad1;
    map<string|error> a4 = <map<string|error>> ad2;

    return a3.get("e1") === e1 && a3.get("e3") === e3 && a4.get("e1") === e1 && 
            a4.get("e2") === e2 && a4.get("e3") === e3 && a3 === a1 && a4 === a2;
}

type MyRecord record {|
    string name;
    anydata|error...;
|};

type MyRecordTwo record {|
    int id;
    error...;
|};

function testRecordsWithErrorsAsAnydata() returns boolean {
    MyRecord m1 = { name: "Anne", "err": e1 };
    MyRecordTwo m2 = { id: 100, "err": e3 };

    anydata a1 = m1;
    anydata a2 = <anydata> m2;

    MyRecord m3 = <MyRecord> a1;
    MyRecordTwo m4 = <MyRecordTwo> a2;

    return m3["err"] === e1 && m4["err"] === e3 && m3 === a1 && m2 === m4;
}

type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
