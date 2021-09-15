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
// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type SomeRecord record {
    int intField;
};

public function testIfElseNarrowTypesRestting1() {
    SomeRecord? c = {intField: 10};
    SomeRecord? f = ();
    SomeRecord|int|() g = 4;
    any x = true;

    if c is () {

    } else if g is () {

    } else if x is int {
        c = f;
        if (f is SomeRecord) {
            hoo(c);
            if (g is int) {
                g = ();
                goo(g);
            } else {
                SomeRecord s = g;
                g = 4;
                yoo(g);
            }
        }
    } else if (x is float) {
         SomeRecord|int s = g;
    } else if (x is boolean) {
        foo(c);
    } else if (x is string) {
        goo(g);
    } else {
        SomeRecord|int w = 4;
        g = w;
        goo(g);
    }
}

function foo(() aa) {

}

function hoo(SomeRecord aa) {

}

function goo(SomeRecord|int aa) {

}

function yoo(SomeRecord|int|() aa) {

}

public function testIfElseNarrowTypesRestting2() {
    int|string? a = 1;
    int|string? b = 1;

    if a is int|string && b is int|string {
        if a is int {

        } else {
            if b is int {
                a = 1;
                b = 1;
            } else {
                string c = a;
                string d = b;
            }
        }

        int|string e = a;
        int|string f = b;
    }
}