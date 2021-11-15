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

type Foo record {
    int x;
};

function fn1() {
    int i = 1;

    match i {
        1 if (let Foo f2 = {x: 1, y: 2} in f2.x == 2) => { // error
        }
        2 if (let Foo f2 = {x: 1, "y": 2} in f2.x == 2) => { // OK
        }
        3 if (let Foo f2 = {z: 0, x: 1, y: 2} in f2.x == 2) => { // error
        }
    }
}

function fn2() {
    Foo f = {x: 1};

    match f {
        var {x, y} if y is int && f is record {int x; string y;} => {
        }
        var {x} if f is record {string x; int i;} || x is 1 => {
        }
        var {x} if x == 2 || f is record {string x; int i;} => {
        }
    }
}
