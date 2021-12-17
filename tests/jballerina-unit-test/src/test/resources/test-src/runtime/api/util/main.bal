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

import ballerina/test;
import ballerina/jballerina.java;

public function main() {
    testIdentifierDecoding();
}

function testIdentifierDecoding() {
    test:assertEquals(decodeIdentifier("üňĩćőđę_ƈȏɳʂʈ_IL"), "üňĩćőđę_ƈȏɳʂʈ_IL");
    test:assertEquals(decodeIdentifier("const_IL_123"), "const_IL_123");
    test:assertEquals(decodeIdentifier(" $0047$0058@$0091`{~_IL"), " /:@[`{~_IL");
}

function decodeIdentifier(string s) returns string = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

