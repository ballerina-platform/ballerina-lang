// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/test;
import ballerina/jballerina.java;

public client isolated class Client {
    resource function get greeting/[int p1]/[string p2]/[boolean pn]/[string... path](int t1, string t2,
            boolean tn = true) returns int = @java:Method {
        name: "resourceGetFunc",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Environment"
    } external;

    resource function post greeting/[int p1]/[decimal p2]/[float pn](int t1, string t2,
            boolean tn = true) returns int = @java:Method {
        name: "resourcePostFunc",
        'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Environment"
    } external;
}

public function main() {
    testGetFunctionNameAndGetPathParameters();
}

function testGetFunctionNameAndGetPathParameters() {
    Client clientResult = new Client();

    int intResult = clientResult->/greeting/[1200]/["apple"]/[true](2400, "orange", false);
    test:assertEquals(intResult, 1);

    int intResult2 = clientResult->/greeting/[600]/[0]/[0.0].post(800, "", false);
    test:assertEquals(intResult2, 2);
}
