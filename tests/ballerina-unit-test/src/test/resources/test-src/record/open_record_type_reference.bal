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

// TESTS FOR RECORDS WHERE THE REFERENCED TYPE ONLY HAS VALUE TYPE FIELDS

type ValType record {
    int ri;
    float rf;
    string rs;
    boolean rb;
    byte ry;
};

type ClosedValType record {
    int cri;
    float crf;
    string crs;
    boolean crb;
    byte cry;
    !...
};

type Foo1 record {
    int a;
    float b;
    *ValType;
    string s;
    *ClosedValType;
};

function testValRefType() returns Foo1 {
    Foo1 f = {a:10, b:23.45, s:"hello foo", ri:20, crs:"qwerty"};
    f.rf = 45.6;
    f.rs = "asdf";
    f.rb = true;
    f.ry = 255;
    f.cri = 20;
    f.crf = 12.34;
    f.crb = true;
    f.cry = 254;
    return f;
}

// TESTS FOR RECORDS WHERE THE REFERENCED TYPE HAS COMPLEX REF TYPE FIELDS

type Person object {
    string name;

    new(name){
    }
};

type Employee record {
    int id;
    string name;
    float salary;
    !...
};

type Address record {
    string city;
    string country;
};

type RefType record {
    json rj;
    xml rx;
    Person rp;
    Address ra;
};

type ClosedRefType record {
    json crj;
    xml crx;
    Person crp;
    Address cra;
    !...
};

type Foo2 record {
    string s;
    *RefType;
    int i;
    *ClosedRefType;
};

function testRefTypes() returns Foo2 {
    Foo2 f = {s:"qwerty", i:10, rx:xml `<book>Count of Monte Cristo</book>`, rp:new("John Doe"), crp:new("Jane Doe")};
    json j = {name: "apple", color: "red", price: 40};
    Address adr = {city:"Colombo", country:"Sri Lanka"};

    f.rj = j;
    f.ra = adr;
    f.crj = j;
    f.cra = adr;
    f.crx = xml `<book>Count of Monte Cristo</book>`;

    return f;
}
