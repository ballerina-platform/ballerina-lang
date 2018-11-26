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

# Represents service endpoint where socket server service registered and start.
#
public type Listener object {

    *AbstractListener;

    public function __init(ListenerConfig config) {
        var result = self.initServer(config);
        if (result is error) {
            panic result;
        }
    }

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {
        return ();
    }

    public function __attach(service s, map<any> annotationData) returns error? {
        return self.register(s, annotationData);
    }

    extern function initServer(ListenerConfig config) returns error?;

    extern function register(service s, map<any> annotationData) returns error?;

    extern function start() returns error?;
};

# Represents the socket server configuration.
#
# + interface - the interface that server with to bind
# + port - the port that server wish to bind
public type ListenerConfig record {
    string? interface = ();
    int port = 0;
    !...
};
