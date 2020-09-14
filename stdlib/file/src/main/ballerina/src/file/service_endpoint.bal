// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/java;

///////////////////////////////////
/// Direcotry Listener Endpoint ///
///////////////////////////////////

# Represents the directory listener endpoint, which is used to listen to a directory in the local file system.
public class Listener {
    private ListenerConfig config;

    *lang:Listener;

    # Creates a new Directory listener.
    #
    # + listenerConfig - The `ListenerConfig` record with the directory details
    public function init(ListenerConfig listenerConfig) {
        self.config = listenerConfig;
        var result = initEndpoint(self);
        if (result is error) {
            panic result;
        }
    }

    # Starts the `file:Listener`.
    #
    # + return - () or else error upon failure to start the listener
    public function __start() returns error? {
        return startEndpoint(self);
    }

    # Stops the `file:Listener` gracefully.
    #
    # + return - () or else error upon failure to stop the listener
        public function __gracefulStop() returns error? {
        return ();
    }

    # Stops the `file:Listener` forcefully.
    #
    # + return - () or else error upon failure to stop the listener
    public function __immediateStop() returns error? {
        return ();
    }

    # Binds a service to the `file:Listener`.
    #
    # + s - Type descriptor of the service
    # + name - Name of the service
    # + return - () or else error upon failure to attach to the service
    public function __attach(service s, string? name = ()) returns error? {
        return register(self, s, name);
    }

    # Stops listening to the directory and detaches the service from the `file:Listener`.
    #
    # + s - Type descriptor of the service
    # + return - () or else error upon failure to detach to the service
    public function __detach(service s) returns error? {
    }
}

# Represents configurations that required for directory listener.
#
# + path - Directory path which need to listen
# + recursive - Recursively monitor all sub folders or not in the given direcotry path
public type ListenerConfig record {|
    string? path = ();
    boolean recursive = false;
|};

function initEndpoint(Listener fileListener) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.service.endpoint.InitEndpoint",
    name: "initEndpoint"
} external;

function register(Listener fileListener, service s, string? name) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.service.endpoint.Register",
    name: "register"
} external;

function startEndpoint(Listener fileListener) returns error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.service.endpoint.Start",
    name: "start"
} external;
