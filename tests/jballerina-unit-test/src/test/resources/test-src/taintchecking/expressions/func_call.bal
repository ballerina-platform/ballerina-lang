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

public function main(int i, string s, float b1, float b2) {
    string untaintedS = <@untainted> s;
    assertEquals(bar(<@untainted> i, untaintedS), "1.str");

    assertEquals(bar(<@untainted> i, untaintedS, <@untainted> b1, <@untainted> b2), "1.str1.0 0.0 ");

    float[] bArr = [b1, b2];
    assertEquals(bar(<@untainted> i, <@untainted> s, ...<@untainted> bArr), "1.str1.0 0.0 ");

    assertEquals(bar(<@untainted> i, untaintedS, <@untainted> b1, ...<@untainted> bArr), "1.str1.0 1.0 0.0 ");

    var w = [s, b1, b2];
    assertEquals(bar(<@untainted> i, ...<@untainted> w), "1.str1.0 0.0 ");

    [int, string, float] x = [i, s, b1];
    assertEquals(bar(...<@untainted> x), "1.str1.0 ");

    [int, string, float, float] y = [i, s, b1, b2];
    assertEquals(bar(...<@untainted> y), "1.str1.0 0.0 ");

    [int, string] z = <@untainted> [i, s];
    assertEquals(bar(...z), "1.str");
    assertEquals(baz(...z), "2.str2");
}

function bar(@untainted int i, @untainted string s, @untainted float... b) returns string {
    string str = string `${i}.${s}`;

    foreach float val in b {
        str += val.toString() + " ";
    }
    return str;
}

function baz(@untainted int i, @untainted string s) returns string {
    return string `${2 * i}.${s}${2}`;
}

function assertEquals(string actual, string expected) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected + "', found '" + actual + "'");
}
