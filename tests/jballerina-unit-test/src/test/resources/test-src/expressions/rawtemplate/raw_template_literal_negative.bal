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
        public (readonly & string[]) strings;
        public int insertions; // invalid insertions field type
    } temp1 = `${x} + ${y} = ${x + y}`;

    object {
        public string strings; // invalid strings field type
        public int[] insertions;
    } temp2 = `${x} + ${y} = ${x + y}`;
}

type Template object {
    public (readonly & string[]) strings;
    public [int, string] insertions;
};

function testSubtyping2() {
    int x = 25;
    string s = "foo";
    float f = 12.34;

    Template t1 = `Using tuples: ${x}, ${s}, ${f}`; // extra insertions
    Template t2 = `Using tuples: ${x}`; // missing insertions

    object {
        public (readonly & string[]) strings;
        public [int, string, anydata...] insertions;
    } temp2 = `Using tuples 2: ${x}, ${s}, ${t1}`;
}

const FOO = "Foo";
const BAR = "Bar";

type FooBar FOO|BAR;

function testSubtyping3() {
    int x = 10;

    object {
        public (FooBar[] & readonly) strings;
        public int[] insertions;
    } temp1 = `Foo ${x}Bar`;
}

class Template1 {
    public (string[] & readonly) strings = [];
    public int[] insertions = [];
}

function testSubtyping4() {
    int x = 10;
    int y = 20;

    Template1 t = `${x} + ${y} = ${x + y}`;
}

function testSubtyping5() {
    int x = 25;
    string s = "foo";
    float f = 12.34;

    object {
        public (string[] & readonly) strings;
        public [anydata...] insertions;
        string name;
    } temp3 = `Using tuples: ${x}, ${s}, ${f}`;
}

function testSubtyping6() {
    object {
        public (readonly & string[]) strings;
        public int[] insertions;
        int name;
    } rt1 = `Hello World!`;

    object {
    } rt2 = `Hello World!`;

    object {
        public (readonly & string[]) strings;
    } rt3 = `Hello World!`;

    object {
        public int[] insertions;
    } rt4 = `Hello World!`;

    object {
        public int[] insertions;
        int foo;
    } rt5 = `Hello World!`;

    object {
        public (readonly & string[]) strings;
        public int[] insertions;

        function shouldNotBeHere();
    } rt6 = `Hello World!`;
}

type Temp1 object {
    public ([string, string, string] & readonly) strings;
    public [int, float] insertions;
};

function testTypeChecking() {
    Temp1 rt1 = `Foo${8}Bar${"asdf"}${23.45}`;
    Temp1 rt2 = `${8}`;
    Temp1 rt3 = `Foo${8}Bar${"asdf"}`;
}

public type Temp2 object {
    public (string[2] & readonly) strings;
    public int[1] insertions;
};

function testFixedLengthArrays() {
    Temp2 t = `Count:${1}${2}`;
    t = `Count`;
}

public type Temp3 object {
    public string[] strings; // non-readonly field
    public int[] insertions;
};

function testReadonlyFieldMismatch() {
    Temp3 t = `Count:${1}${2}`;
}

function testAmbiguousTypes() {
    ob:RawTemplate|Template1 rt = `Count: ${10}, ${20}`;
}
