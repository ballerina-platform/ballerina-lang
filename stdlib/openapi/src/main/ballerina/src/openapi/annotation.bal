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

# Service validation codee
# + contract - OpenApi Contract link
# + tags - OpenApi Tags
# + operations - OpenApi Operations
# + excludeTags - Openapi Validator Off for these tags
# + excludeOperations - Openapi Validator Off for these operations
# + failOnErrors - OpenApi Validator Enable
public type ServiceInformation record {|
    string contract = "";
    string[]? tags = [];
    string[]? operations = [];
    string[]? excludeTags = [];
    string[]? excludeOperations = [];
    boolean failOnErrors = true;

|};

# Configuration elements for client code generation.
#
# + generate - generates client code if set to true
public type ClientInformation record {|
    boolean generate = true;
|};

# Presence of this annotation will mark this endpoint to be used as a service endpoint for client generation
public const annotation ClientEndpoint on source listener;

# Annotation to configure client code generation.
public annotation ClientInformation ClientConfig on service;

# Annotation for additional OpenAPI information of a Ballerina service.
public annotation ServiceInformation ServiceInfo on service;

