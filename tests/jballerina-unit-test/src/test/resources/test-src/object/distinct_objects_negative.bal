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

type Obj0 distinct object {
    function apply() returns int;
};

type Obj1 distinct object {
    *Obj0;
};

type Obj2 object {
    function apply() returns int;
};

type Foo int|distinct object { };

type Bar int|distinct object {
    int i;
};

function f(Obj0 o) {

}

function g(Obj2 o) {

}

function foo() {
    any k = "any-val";
    Obj0 o0 = <Obj0> k;
    Obj1 o1 = <Obj1> k;
    Obj2 o2 = <Obj2> k;

    f(o0);
    f(o1);
    f(o2);

    g(o0);
    g(o1);
    g(o2);
}

public function testDistinctObjectAssignability() {
    object {} x = object {};
    Foo _ = x; // error: incompatible types: expected 'Foo', found 'object { }'
}

public function testDistinctObjectAssignability2() {
    object {
        int i;
    } x = object {
        int i = 1;
    };

    Bar _ = x; // error: incompatible types: expected 'Bar', found 'object { int i; }'

    x = object {
        int i = 10;
    };

    Bar _ = x; // error: incompatible types: expected 'Bar', found 'object { int i; }'
}
