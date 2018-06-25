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

documentation {
    Service configuration. Sets only for client and bidirectional streaming service.

    F{{name}} - Resource name. This applies only for client streaming and bidirectional streaming
                where we can define only one resource. In order to generate proto file, service resource name need to
                pass as annotation parameter.
    F{{clientStreaming}} - Client streaming flag. This applies only for client streaming and
    bidirectional streaming. Flag sets to true, if the service defines as client/bidirectional streaming.
    F{{serverStreaming}} - Server streaming flag. This applies only for bidirectional streaming. Flag
    sets to true, if the service defines as bidirectional streaming.
}
public type GrpcServiceConfig record {
    string name;
    boolean clientStreaming;
    boolean serverStreaming;
};

documentation {
    Service configuration. Sets only for client and bidirectional streaming service.
}
public annotation<service> ServiceConfig GrpcServiceConfig;

documentation {
    Service resource configuration. Sets only for server streaming service.

    F{{streaming}} - Server streaming flag. This flag sets to true to specify that the resource is capable of sending
     multiple responses per request.
}
public type GrpcResourceConfig record {
    boolean streaming;
};

documentation {
    Service resource configuration. Sets only for server streaming service.
}
public annotation<resource> ResourceConfig GrpcResourceConfig;

documentation {
    Service descriptor data. This is for internal use.

    F{{descriptor}} - Service descriptor sets at compile time.
}
public type ServiceDescriptorData record {
    string descriptor;
};

documentation {
    Service descriptor data generated at compile time. This is for internal use.
}
public annotation <service> ServiceDescriptor ServiceDescriptorData;
