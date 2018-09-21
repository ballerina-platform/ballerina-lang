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

# Service configuration. Sets only for client and bidirectional streaming service.
#
# + name - Resource name. This applies only for client streaming and bidirectional streaming
#          where we can define only one resource. In order to generate proto file, service resource name need to
#          pass as annotation parameter.
# + requestType - Request message type of the resource. This is an optional field. If it is not specified, request
#                   type is derived from input argument of the resource.
# + responseType - Response message type of the resource. This is an optional field. If it is not specified, response
#                   type is derived from the he value passed to the send() expression.
# + clientStreaming - Client streaming flag. This applies only for client streaming and
#                     bidirectional streaming. Flag sets to true, if the service defines as client/bidirectional streaming.
# + serverStreaming - Server streaming flag. This applies only for bidirectional streaming. Flag
#                     sets to true, if the service defines as bidirectional streaming.
public type GrpcServiceConfig record {
    string name;
    typedesc requestType;
    typedesc responseType;
    boolean clientStreaming;
    boolean serverStreaming;
    !...
};

# Service configuration. Sets only for client and bidirectional streaming service.
public annotation<service> ServiceConfig GrpcServiceConfig;

# Service resource configuration. Sets only for server streaming service.
#
# + streaming - Server streaming flag. This flag sets to true to specify that the resource is capable of sending
#               multiple responses per request.
# + requestType - Request message type of the resource. This is an optional field. If it is not specified, request
#                   type is derived from input argument of the resource.
# + responseType - Response message type of the resource. This is an optional field. If it is not specified, response
#                   type is derived from the he value passed to the send() expression.
public type GrpcResourceConfig record {
    boolean streaming;
    typedesc requestType;
    typedesc responseType;
    !...
};

# Service resource configuration. Sets only for server streaming service.
public annotation<resource> ResourceConfig GrpcResourceConfig;

# Service descriptor data. This is for internal use.
#
# + descriptor - Service descriptor sets at compile time.
public type ServiceDescriptorData record {
    string descriptor;
    !...
};

# Service descriptor data generated at compile time. This is for internal use.
public annotation <service> ServiceDescriptor ServiceDescriptorData;
