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


///////////////////////////////////
/// Direcotry Listener Endpoint ///
///////////////////////////////////
# Represents directory listener endpoint where used to listen to a directory in the local file system.
#
public type Listener object {
    private ListenerConfig config;

    *AbstractListener;

    public function __init(ListenerConfig listenerConfig) {
        self.config = listenerConfig;
        var result = self.initEndpoint();
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

    extern function initEndpoint() returns error?;

    extern function register(service serviceType, map<any> annotationData) returns error?;

    extern function start() returns error?;
};

# Represents configurations that required for directory listener.
#
# + path - Directory path which need to listen
# + recursive - Recursively monitor all sub folders or not in the given direcotry path
public type ListenerConfig record {
    string? path = ();
    boolean recursive = false;
    !...;
};
