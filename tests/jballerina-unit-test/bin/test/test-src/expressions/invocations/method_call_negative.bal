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

function testInvalidArgsViaVararg() {
    Foo f = new;

    var a = [1, true, false];
    [float, string...] b = [1.0, "str"];

    string s = f.bar(1, ...a); // 16: incompatible types: expected 'string', found 'int'
                               // 28: incompatible types: expected 'boolean[]', found '[int,boolean,boolean]'
    int i = f.bar(1, ...["hello", false]); // 26: incompatible types: expected 'boolean', found 'string'

    _ = f.bar(1, false, ...[1, "world"]); // 29: incompatible types: expected 'boolean', found 'int'
                                          // 32: incompatible types: expected 'boolean', found 'string'
    f.bar(...b);  // 5: variable assignment is required
                  // 14: incompatible types: expected '[int,boolean...]', found '[float,string]'

    f->baz(1, ...a);   // 12: incompatible types: expected 'string', found 'int'
                       // 18: incompatible types: expected '[float,boolean...]', found '[int,boolean,boolean]'
    f->baz(...b);  // 15: incompatible types: expected '[string,float,boolean...]', found '[float,string...]'
}

function testInvalidIndividualArgsWithVararg() {
    Foo f = new;

    boolean[] a = [true, false];

    _ = f.bar(i = 1, ...a); // 22: rest argument not allowed after named arguments

    _ = f->baz("str", ...a); // 26: incompatible types: expected '[float,boolean...]', found 'boolean[]'

    var fn = function () returns [boolean, boolean...] {
        return [true, false, false];
    };

    int i = f.bar(1, 1.0, ...fn()); // 22: incompatible types: expected 'boolean', found 'float'
}

client class Foo {
    function bar(int i, boolean... b) returns int {
        return i;
    }

    remote function baz(string s, float f = 2.0, boolean... b) {
    }
}
