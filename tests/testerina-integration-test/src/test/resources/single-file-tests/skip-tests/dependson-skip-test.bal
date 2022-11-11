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

// Tests skipping of dependsOn functions when a test func fails.

int j = 0;

// This test should pass
@test:Config {}
public function test1() {
    j = j+1;
}

// This test should fail and the consecutive depends on tests will be skipped
@test:Config {
    dependsOn: [test1]
}
public function test2() {
    int i = 12/0;
}

@test:Config {
    dependsOn: [test2]
}
public function test3() {
    j = j+1;
}

@test:Config {
    dependsOn: [test3]
}
public function test4() {
    j = j+1;
}

// This test should pass
@test:Config {}
public function test5() {
    j = j+1;
    test:assertEquals(j, 2);
}


// public function __execute__( string targetPath, string packageName, string moduleName, string report, string coverage, string groups, string disableGroups, string tests, string rerunFailed, string listGroups) returns error? {
// test:setTestOptions(targetPath,packageName,moduleName,report,coverage,groups,disableGroups,tests,rerunFailed,listGroups);
// test:registerTest("test1",test1);
// test:registerTest("test2",test2);
// test:registerTest("test3",test3);
// test:registerTest("test4",test4);
// test:registerTest("test5",test5);
// test:startSuite();
// }