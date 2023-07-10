// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import bir/objs;

function f1(objs:Bar bar) {
    assertEquality(bar is objs:Foo, false);
}

function f2(objs:Foo foo) {
    assertEquality(foo is objs:Bar, false);
}

function f3(objs:Xyz xyz) {
    assertEquality(xyz is objs:Qux, false);
}

function f4(objs:Qux qux) {
    assertEquality(qux is objs:Xyz, false);
}

function assertEquality(any|error actual, any|error expected) {
}
