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
type Foo2 int|(readonly & distinct object { });
type Foo3 (distinct object { } & readonly)|distinct object { };
type Foo4 distinct object { }|distinct object { }|distinct object { };
type Foo5 distinct object { }|distinct error;

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
    f(o2); //error: incompatible types: expected 'Obj0', found 'Obj2'

    g(o0);
    g(o1);
    g(o2);
}

public function testDistinctObjectAssignability() {
    object {
        int i;
    } x = object {
        int i = 1;
    };

    Bar _ = x; //error: incompatible types: expected 'Bar', found 'object { int i; }'

    x = object {
        int i = 10;
    };

    Bar _ = x; //error: incompatible types: expected 'Bar', found 'object { int i; }'
}

public function testDistinctObjectAssignability2() {
    object {} x = object {};
    Foo f1 = x; //error: incompatible types: expected 'Foo', found 'object { }'
    Foo2 f2 = x; //error: incompatible types: expected 'Foo2', found 'object { }'
    Foo3 f3 = x; //error: incompatible types: expected 'Foo3', found 'object { }'
    Foo4 f4 = x; //error: incompatible types: expected 'Foo4', found 'object { }'
    Foo5 f5 = x; //error: incompatible types: expected 'Foo5', found 'object { }'

    distinct object { } _ = f1; //error: incompatible types: expected 'object { }', found 'Foo'
    distinct object { } _ = f2; //error: incompatible types: expected 'object { }', found 'Foo2'
    distinct object { } _ = f3; //error: incompatible types: expected 'object { }', found 'Foo3'
    distinct object { } _ = f4; //error: incompatible types: expected 'object { }', found 'Foo4'
    distinct object { } _ = f5; //error: incompatible types: expected 'object { }', found 'Foo5'

    f1 = f2; //error: incompatible types: expected 'Foo', found 'Foo2'
    f1 = f3; //error: incompatible types: expected 'Foo', found 'Foo3'
    f1 = f4; //error: incompatible types: expected 'Foo', found 'Foo4'
    f1 = f5; //error: incompatible types: expected 'Foo', found 'Foo5'
    f2 = f1; //error: incompatible types: expected 'Foo2', found 'Foo'
    f2 = f3; //error: incompatible types: expected 'Foo2', found 'Foo3'
    f2 = f4; //error: incompatible types: expected 'Foo2', found 'Foo4'
    f2 = f5; //error: incompatible types: expected 'Foo2', found 'Foo5'
    f3 = f1; //error: incompatible types: expected 'Foo3', found 'Foo'
    f3 = f2; //error: incompatible types: expected 'Foo3', found 'Foo2'
    f3 = f4; //error: incompatible types: expected 'Foo3', found 'Foo4'
    f3 = f5; //error: incompatible types: expected 'Foo3', found 'Foo5'
    f4 = f1; //error: incompatible types: expected 'Foo4', found 'Foo'
    f4 = f2; //error: incompatible types: expected 'Foo4', found 'Foo2'
    f4 = f3; //error: incompatible types: expected 'Foo4', found 'Foo3'
    f4 = f5; //error: incompatible types: expected 'Foo4', found 'Foo5'
}
