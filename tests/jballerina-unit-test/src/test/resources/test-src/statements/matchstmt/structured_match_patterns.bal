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


type Foo record {
    string s;
    int i;
    [float, int, boolean] fib;
};

function testStructuredMatchPatternsBasic1() returns string {
    Foo foo = {s: "S", i: 23, fib: [5.6, 3, true]};

    match foo {
        var {s, i: integer, fib: [a, b, c]} => {
            integer += 1;
            a += 1;
            b += 1;
            return "Matched Values : " + s + ", " + integer.toString() + ", " + a.toString() + ", " + b.toString() +
                    ", " + c.toString();
        }
    }

    return "Default";
}

type Foo2 record {
    string s;
    int i;
    float f;
};

type Bar record {
    byte b;
    Foo2 f;
};

function testStructuredMatchPatternsBasic2() returns string {
    Foo2 foo = {s: "S", i: 23, f: 5.6};
    Bar bar = {b: 12, f: foo};
    [Foo2, [int, Bar], byte] tuple = [foo, [100, bar], 200];
    match tuple {
        var [{s, i, f}, [i2, {b, f: {s: s2, i: i3, f: f2}}], b2] => {
            i3 += 1;
            return "Matched Values : " + s + ", " + i.toString() + ", " + f.toString() + ", " + i2.toString() + ", " +
                        b.toString() + ", " + s2 + ", " + i3.toString() + ", " + f2.toString() + ", " + b2.toString();
        }
    }

    return "Default";
}

function testErrorShouldNotMatchWildCardPatternVarIgnore() returns string {
    any|error v = error("{UserGenError}Error");
    match v {
        0 => { return "zero"; }
        var _ => { return "other"; }
    }
    return "no-match";
}

function testErrorNotMatchingVarIgnoreAndFallThroughToErrorPattern() returns string {
    any|error v = error("{UserGenError}Error");
    match v {
        0 => { return "zero"; }
        var _ => { return "other"; }
        error(var r) => { return <string>r; }
    }
    return "no-match";
}
