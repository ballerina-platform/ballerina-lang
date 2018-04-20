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
documentation {
    Represents directory listener endpoint where used to listen to a directory in the local file system.

    F{{config}} - ListenerEndpointConfiguration configurations.
}
public type Listener object {
    private {
        ListenerEndpointConfiguration config;
    }

    public function init(ListenerEndpointConfiguration config) {
        self.config = config;
        check self.initEndpoint();
    }

    native function initEndpoint() returns error?;

    public native function register(typedesc serviceType);

    public native function start();

    public native function stop();
};

documentation {
    Configuration for direcotry listener endpoint.

    F{{path}} - Directory path which need to listern.
    F{{recursive}} - Recursively monitor all sub folders or not in the given direcotry path.
}
public type ListenerEndpointConfiguration {
    string path,
    boolean recursive,
};
