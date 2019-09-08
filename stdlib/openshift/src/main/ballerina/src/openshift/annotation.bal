// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public const string BUILD_EXTENSION_OPENSHIFT = "openshift";

# Domain for OpenShift Route configuration.
#
# + domain - The domain of the hostname.
public type RouteDomainConfig record {|
    string domain;
|};

# Route configuration for @kubernetes:OpenShiftRoute.
#
# + name - Name of the resource
# + labels - Map of labels for the resource
# + annotations - Map of annotations for resource
# + host - The host of the route.
public type RouteConfiguration record {|
    string name?;
    map<string> labels?;
    map<string> annotations?;
    string|RouteDomainConfig host;
|};

# @kubernetes:OpenShiftRoute annotation to generate openshift routes.
public const annotation RouteConfiguration Route on source service, source listener;
