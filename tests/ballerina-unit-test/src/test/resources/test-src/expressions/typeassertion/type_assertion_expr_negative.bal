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

function testAssertionWithUnassignableTypes() {
    Def def = { name: "Em", id: 123.4 };
    Abc abc = <Abc> def;

    map<int> m1 = {};
    map<any> m2 = <map<any>> m1;
}

function testAssertionForCurrentlyUnsupportedTypes() {
    future<int> f1 = start testFutureFunc();
    any a = f1;
    future<int> f2 = <future<int>> a;
}

type Abc record {
    string name;
    int id;
};

type Def record {
    json name;
    float id;
};

type Employee record {
    string name;
    !...;
};

function testFutureFunc() returns int {
    return 1;
}
