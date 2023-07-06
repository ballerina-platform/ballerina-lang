// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/typereftypes as tr;

type ImmutableIntOrStringArray tr:Strings2[]|tr:ImmutableIntArray2;
type FunctionType1 function (tr:Integer2 i) returns tr:Decimal2;

type MapFooBar map<tr:FooBar2>;

record {|
    tr:ImmutableIntArray2 a;
|} X = {a : [1]};

record {|
    *tr:Bar2;
|} Y = {b : {}};

function testFn() {
    tr:Baz baz = {c: ()};
    tr:Corge corge = ();

    boolean b1 = true;
    if b1 {
        tr:FooBar2 b = "foo";
    } else {
        tr:FooBar2 b = 1;
    }

    tr:IntArray2 array = [1, 2];
    foreach tr:Integer2 i in array {
        tr:Integer2 b = i;
    }
}

function getImmutable(ImmutableIntOrStringArray x) returns tr:ImmutableIntArray2 {
    if x is tr:ImmutableIntArray2 {
        return <tr:ImmutableIntArray2> x;
    }
    return [];
}
