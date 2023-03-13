// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const idNum = 1;

type Foo record {
    readonly map<string> m = {};
    int age;
};

type GlobalTable table<Foo> key(m);

type Foo2 record {
    readonly int m = 1;
    string name;
};

type GlobalTable2 table<Foo2> key(m);

type Foo3 record {
    readonly int m = 1;
    readonly string name;
};

type GlobalTable3 table<Foo3> key(m, name);

type Foo4 record {
    readonly int m = 1;
    readonly string name = "A";
};

type GlobalTable4 table<Foo4> key(m, name);

function testTableConstructExprWithDefaultKeys() {
    GlobalTable _ = table [
        {m: {}, age: 21},
        {age: 12}
    ];

    GlobalTable2 _ = table [
        {m: 2, name: "A"},
        {name: "B"}
    ];

    GlobalTable3 _ = table [
        {m: 2, name: "A"},
        {name: "B"}
    ];

    table<record {readonly int idNum = 2; string name;}> key (idNum) _ = table [
        {idNum, name: "Jo"},
        {name: "Chiran"}
    ];

    table<record {readonly int idNum = 2; readonly string name;}> key (idNum, name) _ = table [
        {idNum, name: "Jo"},
        {name: "Chiran"}
    ];

    table<record {readonly int idNum = 2; readonly string name = "A";}> key (idNum, name) _ = table [
        {idNum, name: "Jo"},
        {name: "Chiran"},
        {idNum: 3},
        {}
    ];
}
