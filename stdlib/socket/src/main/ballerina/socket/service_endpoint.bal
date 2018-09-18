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



public type Listener object {

    public extern function init(ListenerEndpointConfiguration config);

    public extern function register(typedesc serviceType);

    public extern function start();

    public extern function getCallerActions() returns CallerAction;
};

public type ListenerEndpointConfiguration record {
    string? interface,
    int port,
};

public type CallerAction object {

    public extern function read(int size) returns byte[]|error;

    public extern function write(byte[] content) returns error?;
};

public type TCPSocketMeta record {
    int remotePort,
    int localPort,
    string remoteAddress,
    string localAddress,
};
