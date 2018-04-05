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
package ballerina.grpc;

@Description {value:"gRPC Service Stub for outbound gRPC requests"}
public type ServiceStub object {
    public {
        Client client;
    }

    @Description {value:"init native function for initialize the Stub."}
    @Param {value: "Service Stub type. possible values: blocking, nonblocking"}
    @Param {value: "Proto descriptor key. Key of proto descriptor"}
    @Param {value: "Proto descriptor map. descriptor map with all dependent descriptors"}
    public native function initStub (any clientEndpoint, string stubType, string descriptorKey, map descriptorMap);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    public native function blockingExecute (string methodID, any payload) returns (any | ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    public native function nonBlockingExecute (string methodID, any payload, typedesc listenerService) returns
    (ConnectorError);

    @Description {value:"The execute action implementation of the gRPC Connector."}
    @Param {value:"Connection stub."}
    @Param {value:"Any type of request parameters."}
    public native function streamingExecute (string methodID, typedesc listenerService) returns (Client |
            ConnectorError);
}


@Description {value:"Represents the gRPC client connector connection"}
@Field {value:"host: The server host name"}
@Field {value:"port: The server port"}
public type ClientConnection object {
    public {
        int port;
        string host;
    }

        @Description {value:"Sends request message to the server"}
    @Param {value:"res: The inbound request message"}
    @Return {value:"Error occured when sending the request"}
    public native function send (any res) returns (ConnectorError);

    @Description {value:"Informs the server, caller finished sending messages."}
    @Return {value:"Error occured when sending the complete request"}
    public native function complete () returns (ConnectorError);

    @Description {value:"Sends error response to the server"}
    @Param {value:"clientError: The error message message"}
    @Return {value:"Error occured when sending the error message"}
    public native function errorResponse (ClientError clientError) returns (ConnectorError);
}


