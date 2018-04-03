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

@Description { value:"gRPC service configuration"}
@Field {value:"port: gRPC service listening port"}
@Field {value:"rpcEndpoint: gRPC resource name. This applies only for client streaming and bidirectional streaming
where we can define only one resource. In order to generate proto file, we need resource name."}
@Field {value:"clientStreaming: gRPC client streaming service flag. This applies only for servicestub streaming and
bidirectional streaming. Flag sets to true, if the service is client/bidirectional streaming."}
@Field {value:"serverStreaming: gRPC server streaming service flag. This applies only for client streaming and
bidirectional streaming. Flag sets to true, if the service is bidirectional streaming."}
public struct ServiceConfig {
    Service[] endpoints;
    string rpcEndpoint;
    boolean clientStreaming;
    boolean serverStreaming;
    boolean generateClientConnector;
}

@Description {value:"HTTP Configuration for service"}
public annotation <service> serviceConfig ServiceConfig;

public struct ResourceConfig {
    boolean streaming;
}
public annotation <resource> resourceConfig ResourceConfig;

@Description { value:"Identify the service as server message listener"}
public struct MessageListener {
}

@Description {value:"HTTP Configuration for service"}
public annotation <service> messageListener MessageListener;
