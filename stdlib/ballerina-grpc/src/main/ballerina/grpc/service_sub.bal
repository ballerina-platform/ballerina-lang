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
public struct ServiceStub {
    Client client;
}

@Description {value:"init native function for initialize the Stub."}
@Param {value: "Service Stub type. possible values: blocking, nonblocking"}
@Param {value: "Proto descriptor key. Key of proto descriptor"}
@Param {value: "Proto descriptor map. descriptor map with all dependent descriptors"}
public native function<ServiceStub ep> initStub (any clientEndpoint, string stubType, string descriptorKey, map
                                                                                                            descriptorMap);

@Description {value:"The execute action implementation of the gRPC Connector."}
@Param {value:"Connection stub."}
@Param {value:"Any type of request parameters."}
public native function<ServiceStub ep>  blockingExecute (string methodID, any payload) returns (any | ConnectorError);

@Description {value:"The execute action implementation of the gRPC Connector."}
@Param {value:"Connection stub."}
@Param {value:"Any type of request parameters."}
public native function<ServiceStub ep>  nonBlockingExecute (string methodID, any payload, typedesc listenerService)
returns (ConnectorError);

@Description {value:"The execute action implementation of the gRPC Connector."}
@Param {value:"Connection stub."}
@Param {value:"Any type of request parameters."}
public native function<ServiceStub ep>  streamingExecute (string methodID, typedesc listenerService)
returns (Client | ConnectorError);

@Description {value:"Represents the gRPC client connector connection"}
@Field {value:"host: The server host name"}
@Field {value:"port: The server port"}
public struct ClientConnection {
    int port;
    string host;
}

@Description {value:"Sends outbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ClientConnection conn> send (any res) returns (ConnectorError);

@Description {value:"Informs the caller, server finished sending messages."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ClientConnection conn> complete () returns (ConnectorError);

@Description {value:"Forwards inbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The inbound response message"}
@Return {value:"Error occured during HTTP server connector forward"}
public native function <ClientConnection conn> errorResponse (ClientError clientError) returns (ConnectorError);


