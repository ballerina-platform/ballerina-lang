// Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

type Foo record {||};

type A [int, string];

function testSameTypeDesc() {
    Foo f = {};
    record {|int a;|} r = {a: 1};
    r = {a: 2};

    A a1 = [1, "a"];
    A a2 = [2, "b"];

    var a3 = a1;
    a1 = [3, "c"];

    [int, string] a4 = [1, "a"];
    a4 = [2, "b"];

    typedesc td1 = A;
    typedesc td2 = Foo;

    boolean flag = td1 === td2;

    table<record {|string name;|}> table1 = table [{name: "a"}];
    table1 = table [{name: "b"}];
    var table2 = table1;

    error<record {|string message; int value?;|}> & error e1 = error("error", message = "a", value = 0);
    error e2 = e1;
}
