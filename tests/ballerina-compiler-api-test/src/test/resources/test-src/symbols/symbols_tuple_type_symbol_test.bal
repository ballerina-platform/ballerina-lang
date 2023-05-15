// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

type BuiltInTupType [@Config {count: 1} @HttpConfig int, @Config {count: 2} string, @HttpConfig boolean];

type RecType record {
    int a;
};

type AType string|int;

type BType int;

const BConst = "BB";

type UserDefTupType [@HttpConfig RecType, AType, BType, BConst];

function test() {
    [@TLSConfig @Config {count: 1} string, @HttpConfig AType] tuple = ["String", 10];
}

// utils
type Options record {|
    int count;
|};

const annotation Options Config on field;
const annotation HttpConfig on field;
const annotation TLSConfig on field;
