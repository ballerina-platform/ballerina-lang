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

// Thus the type anydata|error is the supertype of all pure types. The type anydata is equivalent to the union

// () | boolean | int | float | decimal | string
// | (anydata|error)[] | map<anydata|error>
// | xml | table
// TODO: allow (anydata|error)[] and map<anydata|error> as anydata
// https://github.com/ballerina-platform/ballerina-lang/issues/13231 
@test:Config {
    dataProvider: "brokenAnydataValueDataProvider",
    groups: ["deviation"]
}
function testAnydataTypeDescriptorBroken(anydata av) {
    anydata av2 = av;
    test:assertEquals(av2, av, msg = "expected variable to hold the assigned value");
}

function brokenAnydataValueDataProvider() returns anydata[][] {
    return [
        // [anydataOrErrorArray],
        // [anydataOrErrorMap]
    ];
}
