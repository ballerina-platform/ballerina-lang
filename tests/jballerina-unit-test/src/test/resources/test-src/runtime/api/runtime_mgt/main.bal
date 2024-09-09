// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
import ballerina/lang.runtime;
import ballerina/test;
import ballerina/jballerina.java;

int counter = 0;

public function main() {
    testListenerFunctionality();
}

Listener l2 = new Listener(9091);
Listener l3 = new Listener(9092);

function testListenerFunctionality() {
    runtime:registerListener(l2);
    runtime:registerListener(l3);
    Service s = new Service();
    error? result = l3.attach(s);
    runtime:deregisterListener(l2);
    runtime:deregisterListener(l3);
    test:assertTrue(result == ());
    result = l3.start();
    test:assertTrue(result == ());
    test:assertEquals(counter, 3);
    validateArtifactCount();
    validateIsRemoteManagementEnabled();
}

public service class Service {
    public function init() {
        counter += 1;
    }
}

public class Listener {
    *runtime:DynamicListener;
    int port;
    public function 'start() returns error? {
        counter += 1;
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
        counter += 1;
    }

    public function init(int port) {
        self.port = port;
    }

    public function getPort() returns int {
        return self.port;
    }
}

function validateArtifactCount() = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function validateIsRemoteManagementEnabled() = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
