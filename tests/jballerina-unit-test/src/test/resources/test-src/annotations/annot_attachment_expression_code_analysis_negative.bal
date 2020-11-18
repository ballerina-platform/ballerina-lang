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

type Foo1 record {|
    string s;
    int...;
|};

annotation Foo1 f1 on type, class;

@f1 {
    s: "str",
    i: 1 // invalid key 'i': identifiers cannot be used as rest field keys, expected a string literal or an expression
}
class Bar1 {
}

type Foo2 record {
    string s1;
    string? s2;
};

annotation Foo2 f2 on type, class;

@f2 {
    s1: "str",
    s2: null // 'null' literal is only supported for 'json'
}
class Bar2 {

}

type Foo3 record {|
    string s;
    int i;
    float fl;
|};

type Foo4 record {|
    string s;
    int i;
|};

Foo4 fl = {s: "str", i: 2};

annotation Foo3 f3 on type, class;

@f3 {
    i: 1,
    fl: 1.0,
    ...fl, // invalid usage of record literal: duplicate key 'i' via spread operator '...f'
    s: "hi" // invalid usage of record literal: duplicate key 's'
}
class Bar3 {
}
