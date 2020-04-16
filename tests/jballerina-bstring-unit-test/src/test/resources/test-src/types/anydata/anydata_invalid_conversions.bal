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

function testAnydataToValueTypes() returns (int, float, boolean, string, byte) {
    anydata ad = 33;
    int i = check <int>ad;

    ad = 23.45;
    float f = check <float>ad;

    ad = true;
    boolean bool = check <boolean>ad;

    ad = "Hello World!";
    string s = <string>ad;

    ad = 10;
    byte b = check <byte>ad;

    return (i, f, bool, s, b);
}

function testAnydataToJson() returns json {
    json j = { name: "apple", color: "red", price: 40 };
    anydata adJ = j;
    json convertedJ = <json>adJ;
    return convertedJ;
}

function testAnydataToXml() returns xml {
    xml x = xml `<book>The Lost World</book>`;
    anydata adx = x;
    xml convertedX = <xml>adx;
    return convertedX;
}

function testAnydataToRecord() returns (Foo, ClosedFoo) {
    Foo foo = {a: 15};
    anydata adr = foo;
    Foo convertedFoo = <Foo>adr;

    ClosedFoo cFoo = {ca: 20};
    adr = cFoo;
    ClosedFoo convertedCFoo = <ClosedFoo>adr;
    return (convertedFoo, convertedCFoo);
}

function testAnydataToMap() {
    anydata ad;

    map<int> mi;
    ad = mi;
    map<int> convertedMi = <map<int>> ad;

    map<float> mf;
    ad = mf;
    map<float> convertedMf = <map<float>> ad;

    map<boolean> mb;
    ad = mb;
    map<boolean> convertedMb = <map<boolean>> ad;

    map<byte> mby;
    ad = mby;
    map<byte> convertedMby = <map<byte>> ad;

    map<string> ms;
    ad = ms;
    map<string> convertedMs = <map<string>> ad;

    map<json> mj;
    ad = mj;
    map<json> convertedMj = <map<json>> ad;

    map<xml> mx;
    ad = mx;
    map<xml> convertedMx = <map<xml>> ad;

    map<Foo> mr;
    ad = mr;
    map<Foo> convertedMfoo = <map<Foo>> ad;

    map<ClosedFoo> mcr;
    ad = mcr;
    map<ClosedFoo> convertedMCfoo = <map<ClosedFoo>> ad;

    map<table> mt;
    ad = mt;
    map<table> convertedMt = <map<table>> ad;

    map<map<anydata>> mmad;
    ad = mmad;
    map<map<anydata>> convertedMmad = <map<map<anydata>>> ad;

    map<anydata[]> madr;
    ad = madr;
    map<anydata[]> convertedMadr = <map<anydata[]>> ad;
}

function testAnydataToArray() {
    anydata ad;

    int[] ai = [];
    ad = ai;
    int[] convertedAi = <int[]> ad;

    float[] af = [];
    ad = af;
    float[] convertedAf = <float[]> ad;

    boolean[] ab = [];
    ad = ab;
    boolean[] convertedAb = <boolean[]> ad;

    byte[] aby = [];
    ad = aby;
    byte[] convertedAby = <byte[]> ad;

    string[] astr = [];
    ad = astr;
    string[] convertedAs = <string[]> ad;

    json[] aj = [];
    ad = aj;
    json[] convertedAj = <json[]> ad;

    xml[] ax = [];
    ad = ax;
    xml[] convertedAx = <xml[]> ad;

    Foo[] ar = [];
    ad = ar;
    Foo[] convertedAfoo = <Foo[]> ad;

    ClosedFoo[] acr = [];
    ad = acr;
    ClosedFoo[] convertedACfoo = <ClosedFoo[]> ad;

    table[] at = [];
    ad = at;
    table[] convertedAt = <table[]> ad;

    map<anydata>[] amad = [];
    ad = amad;
    map<anydata>[] convertedAmad = <map<anydata>[]> ad;

    anydata[][] aadr = [];
    ad = aadr;
    anydata[][] convertedAadr = <anydata[][]> ad;
}

// Utils

type Foo record {
    int a;
    anydata... // TODO: Remove this line once the default rest field type is changed to anydata
};

type ClosedFoo record {|
    int ca;
|};
