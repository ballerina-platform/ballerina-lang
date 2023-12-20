// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

type Foo record {|
    int i;
    string x;
|};

type Bar record {|
    int j;
    string y;
|};

type Foos record {|
    int i;
    Foo[] fooArr;
|};

type Bars record {|
    int i;
    Bar[] bars;
|};

function foo(Foos fs) {
    int|Bar[] _ = from Foo foo in fs.fooArr
        select {
            j: foo.i
        };
}

function baz(Foos fs, Bars bs) returns Foos => {
    ...fs,
    ["fooArr"]: from var b in bs.bars
                select {
                    i: b.j
                }
};
