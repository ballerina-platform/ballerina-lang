// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.array;
import ballerina/lang.'map;

function testAnyData() {
    int[] x = [];
    boolean[] x1 = x.clone();  // incompatible types: expected 'boolean[]', found 'int[]'
    int[] x2 = x.clone();  // No error;

    (function (string) )[] y = [];
    float[] yy = y.clone(); //  incompatible types: expected 'anydata', found 'function (string) returns ()[]'
}

function testArray() {
    string[] x = ["hello", "world"];
    string x1 = x.pop(); // No error;
    float x2 = x.pop(); // incompatible types: expected 'float', found 'string'

    (record {string x;})[] y = [ { x : "10"}];
    object {
        public function next() returns record {|
            string value;
        |}?;
    } yy = y.iterator();

}

const Foo = "Foo";
const Bar = "Bar";
type FooError error;

function testError1() {
    FooError f = error("Foo");
    boolean f1 = f.message();    // incompatible types: expected 'boolean', found 'string'
    string f2 = f.message(); // No error;
    Foo x1 = f.message(); // incompatible types: expected 'Foo', found 'string'
    Bar x2 = f.message(); // incompatible types: expected 'Bar', found 'Foo'
}

type BarDetail record {
    string message;
    error cause?;
    int id;
};
type BarError error<BarDetail>;

function testError2(){
    BarError b = BarError(Bar, message = "test", id = 10);
    BarDetail b1 = b.detail(); // No error;
    record {| anydata|readonly...; |} b2 = b.detail(); // No error;
    boolean b3 = b.detail(); // incompatible types: expected 'boolean', found 'BarDetail'
}

function testInvalidIterableOpChain() {
    int[] arr = [10, 20, 30, 40];
    string s = arr.'map(function (int v) returns int {
        return v / 10;
    }).reduce(function (string accum, string val) returns string {
        return accum + val;
    }, "");
}

type Person record {
    string name;
};

type Student record {
    string name;
    int age?;
};

function testTypedescFunctions() {
    Student p = { name : "Michel"};
    Person|error q = p.cloneWithType(Person); // No error
    string r = p.cloneWithType(Person); // Error
}

function testInvalidArgForBoundRestParam() {
    byte[] barr = [0, 1];
    barr.push(-1);
    array:push(barr, 256);
    int[] x = [-10, 257];
    array:push(barr, ...x);

    barr.unshift(-3);
    barr.unshift(-3, 257);
    array:unshift(barr, ...x);
}

function testInvalidArgForBoundRequiredParam() {
    map<int> m = {
        a: 1,
        b: 2
    };

    int s = m.reduce(function (int i, byte b) returns int {
                        return i;
                    },
                    1.0);

    s = 'map:reduce(m,
                    function (int i, byte b) returns int {
                        return i;
                    },
                    1.0);

    byte[] barr = [0, 1];
    int? index = barr.indexOf(257);

    int i = -1;
    index = barr.indexOf(i);
}

function testInvalidArgOnUnionTypedValue() {
    int[]|string[] arr = [1, 2];
    arr.push(true);
    array:unshift(arr, 13.2);
}

function testStringIntFloatSimpleAndArrayUnionReturnParameterNarrowing() {
    string[]|int[]|int|float[]|float arr = <int[]>[1, 2];
    if (arr is int[]|float[]|string[]) {
        [int, (int|float)][] y = arr.enumerate();
    }
}
