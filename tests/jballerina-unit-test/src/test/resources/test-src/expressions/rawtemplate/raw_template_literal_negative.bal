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

import ballerina/lang.'object as ob;

function testBasicUsage() {
    string name = "Pubudu";
    string rt1 = `Hello ${name}!`;
    anydata rt2 = `Hello ${name}!`;
}

function testSubtyping1() {
    int x = 10;
    int y = 20;

    object {
        public string[] strings = [];
        public int insertions = 0;
    } temp1 = `${x} + ${y} = ${x + y}`;

    object {
        public string strings = "";
        public int[] insertions = [];
    } temp2 = `${x} + ${y} = ${x + y}`;
}

type Template object {
    public string[] strings = [];
    public [int, string] insertions = [];
};

function testSubtyping2() {
    int x = 25;
    string s = "foo";
    float f = 12.34;

    Template t1 = `Using tuples: ${x}, ${s}, ${f}`;
    Template t2 = `Using tuples: ${x}`;

    object {
        public string[] strings = [];
        public [int, string, anydata...] insertions;
        string name = "";

        function __init() {
            self.insertions = [];
        }
    } temp2 = `Using tuples 2: ${x}, ${s}, ${t1}`;
}

const FOO = "Foo";
const BAR = "Bar";

type FooBar FOO|BAR;

function testSubtyping3() {
    int x = 10;

    object {
        public FooBar[] strings = [];
        public int[] insertions = [];
    } temp1 = `Foo ${x}Bar`;
}
