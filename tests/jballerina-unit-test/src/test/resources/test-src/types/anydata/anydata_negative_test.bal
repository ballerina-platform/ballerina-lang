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

type Foo record {|
    int ra = 0;
    any...;
|};

class Bar {
    int oa = 0;
}

type Employee record {|
    int id;
    string name;
    float salary;
|};

type ValueType int|float|string|boolean|byte;
type DataType ValueType|json|xml|Bar|map<anydata>|anydata[]|();

function testInvalidAssignmentsWithLiterals() {
    anydata|map<int> adrl = {a: 15};
    anydata|map<int|float> adcrl = {ca: 30};
    map<json>|anydata adjl = { name: "apple", color: "red", price: 40 };
    // TODO: Enable the below scenario after https://github.com/ballerina-platform/ballerina-lang/issues/10914 is fixed
    // anydata adtl = table {
    //                        { primarykey id, name, salary },
    //                        [
    //                          { 1, "Mary", 300.5 },
    //                          { 2, "John", 200.5 },
    //                          { 3, "Jim", 330.5 }
    //                        ]
    //                    }; 
}

function testInvalidMapAssignments() {
    anydata ad;

    map<any> m = {};
    ad = m;

    map<any> m2 = {};
    ad = m2;

    map<Bar> mo = {};
    ad = mo;

    map<function (string) returns boolean> mfp = {};
    ad = mfp;

    map<typedesc<any>> mtd = {};
    ad = mtd;

    map<any[]> mar = {};
    ad = mar;

    map<map<map<any>>> mnested = {};
    ad = mnested;

    map<Foo> mr = {};
    ad = mr;

    map<[DataType, string]> mtup = {};
    ad = mtup;

    map<[[DataType, string], Bar]> mtup2 = {};
    ad = mtup2;

    map<DataType> mu = {};
    ad = mu;
}

function testInvalidArrayAssignments() {
    anydata ad;

    Bar[] abar = [];
    ad = abar;

    (function (string) returns boolean)?[] afp = [];
    ad = afp;

    any[] aa = [];
    ad = aa;

    typedesc<any>[] atd = [];
    ad = atd;

    any[][] a2a = [];
    ad = a2a;

    map<any>[] am = [];
    ad = am;

    map<Bar>[] ambar = [];
    ad = ambar;

    Foo[] ar = [];
    ad = ar;

    DataType[] au = [];
    ad = au;

    [[DataType, string], int, float][] atup = [];
    ad = atup;
}

function testInvalidUnionAssignments() {
    DataType dt = "hello world!";
    anydata ad = dt;
}

function testInvalidTupleAssignments() {
    [int, float, Bar] t1 = [10, 23.45, new Bar()];
    anydata ad = t1;

    [DataType, int] t2 = ["hello world!", 10];
    ad = t2;

    [[DataType, int], string, int] t3 = [["hello world!", 10], "foo", 20];
    ad = t3;
}

function testInvalidMapInsertions() {
    map<anydata> m = {};
    m["bar"] = new Bar();

    DataType dt = "hello world!";
    m["datatype"] = dt;
}

function testErrorAsAnydata() {
    error e = error("test err");
    anydata ad = e;
}
