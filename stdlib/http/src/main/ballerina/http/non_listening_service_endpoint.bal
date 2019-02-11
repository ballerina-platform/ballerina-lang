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


# Mock server endpoint which does not open a listening port.
public type MockListener object {

    *AbstractListener;

    private int port = 0;
    private ServiceEndpointConfiguration config = {};

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {
        return self.stop();
    }

    public function __attach(service s, map<any> annotationData) returns error? {
        return self.register(s, annotationData);
    }

    public function __init(int port, ServiceEndpointConfiguration? config = ()) {
        self.config = config ?: {};
        self.port = port;
        self.init(self.config);
    }

    public function init (ServiceEndpointConfiguration c);
    public extern function initEndpoint () returns (error?);
    public extern function register (service s, map<any> annotationData) returns error?;
    public extern function start ();
    public extern function stop ();
};

public function MockListener.init (ServiceEndpointConfiguration c) {
    var err = self.initEndpoint();
    if (err is error) {
        panic err;
    }
}
