// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    int c?;
    int d?;
|};

type Bar record {|
    int a;
    int b;
    int c = 10;
    int d;
|};

type Baz record {|
    int c;
|};

type Val record{|
|};

function getSum(int a, int b, int c, int d = 1) returns int {
    return a + b + c + d;
}

function getAvg(int a, int b, int c) returns int {
    return (a + b + c)/4;
}

function getTotal(int a, int b, int c, int... m) returns int {
    return a + b + c;
}

function testFunctionWithMappingTypeRestArgNegative() {
    Foo f = {c:20, d:15};
    int sum = getSum(10, 15, ...f);
    
    Bar b1 = {a:10, b: 10, c:50, d: 10};
    int avg = getAvg(...b1);
    
    Baz z = {c:10};
    int avg1 = getAvg(10, b = 10, ...z);

    Bar b2 = {a:10, b: 10, c:50, d: 10};
    int total = getTotal(...b2);

    Val v = { };
    int total1 = getTotal(10, 10, 10, ...v);
}
