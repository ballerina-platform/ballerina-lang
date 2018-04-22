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

documentation {
    gRPC service configuration

    F{{name}} - gRPC resource name. This applies only for client streaming and bidirectional streaming
where we can define only one resource. In order to generate proto file, we need resource name.
    F{{clientStreaming}} - gRPC client streaming service flag. This applies only for servicestub streaming and
bidirectional streaming. Flag sets to true, if the service is client/bidirectional streaming.
    F{{serverStreaming}} - gRPC server streaming service flag. This applies only for client streaming and
bidirectional streaming. Flag sets to true, if the service is bidirectional streaming.
}
public type ServiceConfig {
    string name;
    boolean clientStreaming;
    boolean serverStreaming;
};

documentation {
    gRPC service configuration annotation.
}
public annotation<service> serviceConfig ServiceConfig;

documentation {
    gRPC service resource configuration

    F{{streaming}} - gRPC server streaming flag. This flag sets to true when service resource is considered as server
     streaming.
}
public type ResourceConfig {
    boolean streaming;
};

documentation {
    gRPC service resource configuration annotation.
}
public annotation<resource> resourceConfig ResourceConfig;

public type ServiceDescriptorData {
    string descriptor;
};

public annotation <service> ServiceDescriptor ServiceDescriptorData;
