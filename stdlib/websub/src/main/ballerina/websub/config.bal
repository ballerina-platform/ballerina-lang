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

# Record representing the configuration parameters for the WebSub Hub Client Endpoint.
#
# + url - The URL of the target Hub
# + clientSecureSocket - SSL/TLS related options for the underlying HTTP Client
# + auth - Authentication mechanism for the underlying HTTP Client
# + followRedirects - HTTP redirect related configuration
public type HubClientEndpointConfig record {
    string url = "";
    http:SecureSocket? clientSecureSocket = ();
    http:AuthConfig? auth = ();
    http:FollowRedirects? followRedirects = ();
!...
};
