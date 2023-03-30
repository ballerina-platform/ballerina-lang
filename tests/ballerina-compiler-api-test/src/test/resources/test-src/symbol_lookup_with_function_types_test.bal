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

import testorg/testproject;

function testFn() {
    int x = testproject:testFnA(function (int i, float... j) returns string {
            return "";
        });

    testproject:testFnB(function (int i, function (string s, int... t) returns float f) returns string {
                        return "";
                    });
}

public function testingFnA(testproject:FnTypeA fnA) {
}

public function testingFnB(testproject:testFnB fnB) {
}
