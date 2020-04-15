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

# Service configuration.
#
# + name - Service name. This applies only for the dynamic service registration.
# + requestType - Request message type of the resource. This is an optional field. If it is not specified, the request
#                   type is derived from the input argument of the resource
# + responseType - Response message type of the resource. This is an optional field. If it is not specified, the response
#                   type is derived from the value passed to the send() expression
# + clientStreaming - Client streaming flag. This applies only for client streaming and
#                     bidirectional streaming. The flag should be set to true if the service is defined as client/bidirectional streaming
# + serverStreaming - Server streaming flag. This applies only for bidirectional streaming. The flag
#                     should be set to true if the service is defined as bidirectional streaming
public type GrpcServiceConfig record {|
    string name = "";
    typedesc<anydata> requestType?;
    typedesc<anydata> responseType?;
    boolean clientStreaming = false;
    boolean serverStreaming = false;
|};

# Service configuration annotation.
public annotation GrpcServiceConfig ServiceConfig on service;

# Service resource configuration. This should be set only for server streaming services.
#
# + streaming - Server streaming flag. This flag should be set to true to specify that the resource is capable of sending
#               multiple responses per request.
# + requestType - Request message type of the resource. This is an optional field. If it is not specified, request
#                   type is derived from input argument of the resource.
# + responseType - Response message type of the resource. This is an optional field. If it is not specified, response
#                   type is derived from the value passed to the send() expression.
public type GrpcResourceConfig record {|
    boolean streaming = false;
    typedesc<anydata> requestType?;
    typedesc<anydata> responseType?;
|};

# Service resource configuration annotation. This should be set only for server streaming services.
public annotation GrpcResourceConfig ResourceConfig on resource function;

# Service descriptor data generated at the compile time. This is for internal use.
#
# + descriptor - Service descriptor, which should be set at the compile time
# + descMap - Service dependent descriptor map, which should be set at the compile time
public type ServiceDescriptorData record {|
    string descriptor = "";
    map<any> descMap = {};
|};

# Service descriptor annotation. This is for internal use.
public annotation ServiceDescriptorData ServiceDescriptor on service;
