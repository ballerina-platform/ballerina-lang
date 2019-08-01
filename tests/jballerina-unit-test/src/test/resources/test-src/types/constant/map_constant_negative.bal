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

const map<boolean> bm1 = { "key": true };
const map<boolean> bm2 = { "key": bm1["key"] };

const map<float> fm1 = { [getKey()]: getFloatValue() };

function getKey() returns string {
    return "key";
}

function getFloatValue() returns float {
    return 12.5;
}

// Const map update via compound assignment
const map<string> m = { one: "hello", two: "world" };

function testInvalidUpdate() {
    m["one"] += " world";
}
