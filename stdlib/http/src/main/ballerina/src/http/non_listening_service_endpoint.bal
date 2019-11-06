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

import ballerina/lang.'object as lang;

# Mock server endpoint which does not open a listening port.
public type MockListener object {

    *lang:Listener;

    private int port = 0;
    private ListenerConfiguration config = {};

    public function __start() returns error? {
        return self.start();
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

    public function __init(int port, ListenerConfiguration? config = ()) {
        self.config = config ?: {};
        self.port = port;
        self.init(self.config);
    }

    public function init(ListenerConfiguration c) {
        var err = self.initEndpoint();
        if (err is error) {
            panic err;
        }
    }

    public function initEndpoint() returns error? = external;

    public function register(service s, string? name) returns error? = external;

    public function start() returns error? = external;

    public function gracefulStop() returns error? = external;

    public function detach(service s) returns error? = external;
};
