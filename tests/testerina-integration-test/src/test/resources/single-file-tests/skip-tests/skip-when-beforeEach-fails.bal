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

import ballerina/test;

// before function that fails. All test functions should be skipped due to this.

string a = "before";

@test:BeforeEach
public function beforeEach() {
    int i = 12/0;
}

@test:AfterEach
public function AfterEach() {
    a = a + "afterEach";
}

@test:Config {}
public function test1() {
    a = a + "test1";
}

@test:Config {}
public function test2() {
    a = a + "test2";
}

@test:Config {}
public function test3() {
    a = a + "test3";
}

@test:AfterSuite {}
public function afterSuite() {
    test:assertEquals(a, "before");
}
