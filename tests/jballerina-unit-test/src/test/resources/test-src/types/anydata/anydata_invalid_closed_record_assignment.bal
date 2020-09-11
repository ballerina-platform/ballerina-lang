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

function testInvalidRecordAssignment() {
    anydata ad;

    ClosedFoo cFoo = {a: 20};
    ad = cFoo;

    ClosedFoo1 cFoo1 = {ca: 30};
    ad = cFoo1;

    ClosedFoo2 cFoo2 = {ca: 40};
    ad = cFoo2;

    ClosedFoo3 cFoo3 = {ca: 50};
    ad = cFoo3;

    ClosedFoo4 cFoo4 = {ca: 60};
    ad = cFoo4;

    ClosedFoo5 cFoo5 = {ca: 70};
    ad = cFoo5;

    ClosedFoo6 cFoo6 = {ca: 80};
    ad = cFoo6;

    ClosedFoo7 cFoo7 = {ca: 90};
    ad = cFoo7;
}

type ClosedFoo record {|
    int aa = 0;
    any a;
|};

type ClosedFoo1 record {|
    int ca;
    Bar2 b = {};
|};

type ClosedFoo2 record {|
    int ca;
    Bar|() b = ();
|};

type ClosedFoo3 record {|
    int ca;
    function (string) returns boolean fp?;
|};

type ClosedFoo4 record {|
    int ca;
    typedesc<any> td;
|};

type ClosedFoo5 record {|
    int ca;
    map<any> m = {};
|};

type ClosedFoo6 record {|
    int ca;
    map<map<ClosedFoo2>> m = {};
|};

type ClosedFoo7 record {|
    int ca;
    any[] ar = [];
|};

class Bar {
    int oa = 0;
}

type Bar2 record {|
    int ra = 0;
    any...;
|};
