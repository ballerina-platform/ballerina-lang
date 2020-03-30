// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# The context of the gRPC server that passes relevant configuration to the service.
#
# + headers - gRPC headers that pass to a specific service by the client
public type ServerContext object {
    public Headers headers = new;

    # Returns the header value with the specified header name. If there are more than one header value for the
    # specified header name, the first value is returned.
    #
    # + headerName - The header name.
    # + return - Returns first header value if exists, nil otherwise.
    public function getHeader(string headerName) returns string? {
        return self.headers.get(headerName);
    }

    # Returns the context headers passed by the client.
    #
    # + return - context headers passed by the client.
    public function getContextHeaders() returns Headers {
        return self.headers;
    }

    # Checks whether the client specified deadline exceeded or not.
    #
    # + return - Returns true, if caller already closed the connection. false otherwise.
    public function isCancelled() returns boolean {
        return externIsCancelled(self.headers);
    }
};

function externIsCancelled(Headers headers) returns boolean =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.caller.ServerContextUtils"
} external;
