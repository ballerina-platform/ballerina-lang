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

import ballerina/time;

# The context of the gRPC client that passes relevant configuration to the service.
#
# + headers - gRPC headers that pass to a specific service by the client
public type ClientContext object {
    private Headers headers = new;

    # Check whether the requested header exists.
    #
    # + headerName - The header name.
    # + return - Returns true if header exists, false otherwise.
    public function isHeaderExists(string headerName) returns boolean {
        return self.headers.exists(headerName);
    }

    # Returns the header value with the specified header name. If there are more than one header value for the
    # specified header name, the first value is returned.
    #
    # + headerName - The header name.
    # + return - Returns first header value if exists, nil otherwise.
    public function getHeader(string headerName) returns string? {
        return self.headers.get(headerName);
    }

    # Gets all transport headers with the specified header name.
    #
    # + headerName - The header name.
    # + return - Returns header value array.
    public function getAllHeaders(string headerName) returns string[] {
        return self.headers.getAll(headerName);
    }
    # Sets the value of a transport header.
    #
    # + headerName - The header name.
    # + headerValue - The header value.
    public function setHeader(string headerName, string headerValue) {
        self.headers.setEntry(headerName, headerValue);
    }

    # Returns the unique identification of the caller.
    #
    # + headerName - The header name.
    # + headerValue - The header value.
    public function addHeader(string headerName, string headerValue) {
        self.headers.addEntry(headerName, headerValue);
    }

    # Removes a transport header from the request.
    #
    # + headerName - The header name.
    public function removeHeader(string headerName) {
        self.headers.remove(headerName);
    }

    # Removes all transport headers from the message.
    public function removeAllHeaders() {
        self.headers.removeAll();
    }

    # Set a deadline to the current request.
    #
    # + deadline - A specific point in time that the server needs to terminate.
    public function setDeadline(time:Time deadline) {
        self.headers.addEntry("DEADLINE", time:toString(deadline));
    }

    # Returns the client context headers.
    #
    # + return - client context headers.
    public function getContextHeaders() returns Headers {
        return self.headers;
    }
};
