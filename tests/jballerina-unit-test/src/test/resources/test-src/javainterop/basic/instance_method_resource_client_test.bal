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

import ballerina/jballerina.java;
import ballerina/test;

public client isolated class Client {

    public isolated function init() {

    }

    isolated function newFoo() returns handle = @java:Constructor {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods"
    } external;

    isolated resource function get xyz/[string p1]/[string p2] (handle h) returns int = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods",
        name: "getResource"
    } external;

    isolated resource function get abc/[string p1]/[string p2] (handle h, int i) returns string = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.InstanceMethods",
        name: "getResourceWithBundledPaths",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject",
                        "io.ballerina.runtime.api.values.BArray", "long"]
    } external;
}

public function testInstanceMethodResourceClient() {
    Client cl = new Client();

    handle h = cl.newFoo();
    int i = cl->/xyz/["asd"]/jef.get(h);
    test:assertEquals(i, 5);

    string s = cl->/abc/asd/jef.get(h, 10);
    test:assertEquals(s, "resource method input: 10");
}
