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

type ONE 1;
type TWO 2;
type HELLO "hello";
type ONE_HELLO [ONE, HELLO];
type ONE_TWO [ONE, TWO];

const ONEC = 1;
const HI_C = "hi";

function testNumericLiteralInferring() {
    final var x = 2;
    ONE y = x;
}

function testStringLiteralInferring() {
    final var x = "hi";
    HELLO hello = x;
}

function testListLiteralInferring() {
    final var x = [1, 3];
    final var y = [1, "hi"];
    ONE_TWO ot = x;
    ONE_HELLO oh = y;
}

function testNumericConstInferring() {
    final var x = 2;
    ONEC oc = x;
}

function testStringConstInferring() {
    final var x = "ballerina";
    HI_C hc = x;
}
