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

# Starts all the services defined in the module specified in the 'moduleName' argument.
#
# + moduleName - Name of the module
#
# + return - `true` if the services were started successfully, `false` otherwise
public function startServices(string moduleName) returns boolean = external;

# Stops all the services defined in the module specified in the 'moduleName' argument.
#
# + moduleName - Name of the module
public function stopServices(string moduleName) = external;

# Start a service skeleton from a given OpenApi definition in the given ballerina module.
#
# + moduleName - Name of the module
# + openApiFilePath - Path to the OpenApi definition
#
# + return - `true` if the service skeleton was started successfully, `false` otherwise
public function startServiceSkeleton(string moduleName, string openApiFilePath) returns boolean = external;

# Stop a service skeleton and cleanup created directories of a given ballerina module.
#
# + moduleName - Name of the module
public function stopServiceSkeleton(string moduleName) = external;
