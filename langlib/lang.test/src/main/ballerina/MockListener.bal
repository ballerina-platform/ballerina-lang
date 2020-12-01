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

import ballerina/java;

public type ListenerConfiguration record {|
    string host = "0.0.0.0";
    string httpVersion = "1.1";
    string? server = ();
    boolean webSocketCompressionEnabled = true;
|};

public class MockListener {

    private int port = 0;
    private ListenerConfiguration config = {};

    public function __start() returns error? {
        return self.startEndpoint();
    }

    public function __gracefulStop() returns error? {
        
    }

    public function __immediateStop() returns error? {
        
    }

    public function __attach(service s, string? name = ()) returns error? {
        return self.register();
    }

    public function __detach(service s) returns error? {
        return self.detach();
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
        return externMockInitEndpoint();
    }

    public function register() returns error? {
        return externMockRegister();
    }

    public function startEndpoint() returns error? {
        return externMockStart();
    }

    public function detach() returns error? {
        return externMockDetach();
    }
}

public client class Caller {
    public remote function respond(string message) returns string {
        return message;
    }
     public remote function badRequest(string message) returns string {
        string response = message;
        return self->respond(response);
    }
}

function externMockInitEndpoint() = @java:Method {
    'class: "org.ballerinalang.langlib.test.InitEndPoint",
    name: "initEndPoint"
} external;

function externMockRegister() = @java:Method {
    'class: "org.ballerinalang.langlib.test.Register",
    name: "register"
} external;

function externMockStart() = @java:Method {
    'class: "org.ballerinalang.langlib.test.Start",
    name: "start"
} external;

function externMockDetach() = @java:Method {
    'class: "org.ballerinalang.langlib.test.DetachEndpoint",
    name: "detach"
} external;
