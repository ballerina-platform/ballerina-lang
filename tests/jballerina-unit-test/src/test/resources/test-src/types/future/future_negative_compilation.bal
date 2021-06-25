// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testIncompatibleTypes() {
    future<Bar> f1 = start getFoo();
    future<int|string|error> f2 = start getFooOrBar();

    future<Foo> f3 = f1;
    future<int|string> f4 = f2;

    future f5 = f1;
    future<Bar> f6 = f5;
}

type Foo record {
    int i;
};

type Bar record {|
    float f;
|};

function getFoo() returns Foo => {i: 1, "j": "hello"};

function getFooOrBar() returns Foo|Bar => {i: 1, "j": "hello"};
