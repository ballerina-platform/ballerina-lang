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

import ballerina/test;

// For an immutable value, looking like a type and belonging to a type are the same thing.
@test:Config {}
function testLooksLikeAndBelongsToOfImmutableValues() {
    map<string|int> mutableMap = {
        fieldOne: "valueOne",
        fieldTwo: "valueTwo"
    };
    var immutableMap = mutableMap.freeze();

    if !(immutableMap is map<string>) {
        test:assertFail(msg = "expected immutable value to belong to type map<string>");
    }

    var result = trap map<string>.stamp(immutableMap);
    if result is error {
        test:assertFail(msg = "expected immutable value to look like map<string>");
    }
}
