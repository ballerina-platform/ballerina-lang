// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import specialchars/pkg;
import specialchars/pkg.mod;

function testTypeNameWithSpecialChars() {
    mod:Bdd a = true;
    pkg:BddMemo b = {bdd: a};
    assertEquality(true, b.bdd);
    assertEquality((), b.isEmpty);

    mod:Node c = {
        atom: 0,
        left: true,
        middle: true,
        right: false
    };
    pkg:BddMemo d = {isEmpty: false, bdd: c};
    assertEquality(c, d.bdd);
    assertEquality(false, d.isEmpty);

    mod:Bdd[] e = [a, c];
    any f = e;
    pkg:Foo x = {bar: {i: 0, j: true}};
    assertEquality(false, f is record {| int atom; mod:Bdd left; mod:Bdd middle; mod:Bdd right; |}[]);
    assertEquality(true, f is (record {| int atom; mod:Bdd left; mod:Bdd middle; mod:Bdd right; |}|boolean)[]);
    assertEquality(true, x is (record {|  mod:Bar bar;|}));
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
