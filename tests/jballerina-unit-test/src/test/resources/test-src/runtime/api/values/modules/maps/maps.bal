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

import ballerina/jballerina.java;
import ballerina/test;

public function validateAPI() {
    anydata mapAnydataVal1 = getMapValue();
    test:assertTrue(mapAnydataVal1 is map<anydata>);
    test:assertEquals(mapAnydataVal1, {"a":5});

    map<anydata> mapAnydataVal2 = getMapValueWithInitialValues();
    test:assertEquals(mapAnydataVal2, {"aa": "55", "bb": "66"});
}

function getMapValue() returns anydata = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getMapValueWithInitialValues() returns map<anydata> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
