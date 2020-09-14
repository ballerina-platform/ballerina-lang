// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;
import ballerina/lang.'object as lang;

# Mock server endpoint which does not open a listening port.
public class MockListener {

    *lang:Listener;

    private int port = 0;
    private ListenerConfiguration config = {};

    public function __start() returns error? {
        return self.startEndpoint();
    }

    public function __gracefulStop() returns error? {
        return self.gracefulStop();
    }

    public function __immediateStop() returns error? {
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function __detach(service s) returns error? {
        return self.detach(s);
    }

    public function init(int port, ListenerConfiguration? config = ()) {
        self.config = config ?: {};
        self.port = port;
        var err = self.initEndpoint();
        if (err is error) {
            panic err;
        }
    }

    public function initEndpoint() returns error? {
        return externMockInitEndpoint(self);
    }

    public function register(service s, string? name) returns error? {
        return externMockRegister(self, s, name);
    }

    public function startEndpoint() returns error? {
        return externMockStart(self);
    }

    public function gracefulStop() returns error? {
        return externMockGracefulStop(self);
    }

    public function detach(service s) returns error? {
        return externMockDetach(self, s);
    }
}

function externMockInitEndpoint(MockListener mockListener) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.mock.nonlistening.NonListeningInitEndpoint",
    name: "initEndpoint"
} external;

function externMockRegister(MockListener mockListener, service s, string? name) returns error? = @java:Method {
   'class: "org.ballerinalang.net.http.mock.nonlistening.NonListeningRegister",
   name: "register"
} external;

function externMockStart(MockListener mockListener) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.mock.nonlistening.NonListeningStart",
    name: "start"
} external;

function externMockGracefulStop(MockListener mockListener) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.mock.nonlistening.NonListeningGracefulStop",
    name: "gracefulStop"
} external;

function externMockDetach(MockListener mockListener, service s) returns error? = @java:Method {
    'class: "org.ballerinalang.net.http.mock.nonlistening.NonListeningDetachEndpoint",
    name: "detach"
} external;
