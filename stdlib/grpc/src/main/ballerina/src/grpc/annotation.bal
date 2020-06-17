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
public type GrpcServiceConfig record {|
    string name = "";
|};

# Service configuration annotation.
public annotation GrpcServiceConfig ServiceConfig on service;

# Service descriptor data generated at the compile time. This is for internal use.
#
# + descriptor - Service descriptor, which should be set at the compile time
# + descMap - Service dependent descriptor map, which should be set at the compile time
public type ServiceDescriptorData record {|
    string descriptor = "";
    map<anydata> descMap = {};
|};

# Service descriptor annotation. This is for internal use.
public annotation ServiceDescriptorData ServiceDescriptor on service;
