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

import ballerina/java;

# Provides actions to read/write header values in gRPC request/response message.
public type Headers object {

    # Check whether the requested header exists.
    #
    # + headerName - The header name.
    # + return - Returns true if header exists, false otherwise.
    public function exists(string headerName) returns boolean {
        return externExists(self, java:fromString(headerName));
    }

    # Returns the header value with the specified header name. If there are more than one header value for the
    # specified header name, the first value is returned.
    #
    # + headerName - The header name.
    # + return - Returns first header value if exists, nil otherwise.
    public function get(string headerName) returns string? {
        handle? result = externGet(self, java:fromString(headerName));
        if (result is ()) {
            return result;
        } else {
            return java:toString(result);
        }
    }

    # Gets all transport headers with the specified header name.
    #
    # + headerName - The header name.
    # + return - Returns header value array.
    public function getAll(string headerName) returns string[] {
        return externGetAll(self, java:fromString(headerName));
    }

    # Sets the value of a transport header.
    #
    # + headerName - The header name.
    # + headerValue - The header value.
    public function setEntry(string headerName, string headerValue) {
        return externSetEntry(self, java:fromString(headerName), java:fromString(headerValue));
    }

    # Adds the specified key/value pair as an HTTP header to the request.
    #
    # + headerName - The header name.
    # + headerValue - The header value.
    public function addEntry(string headerName, string headerValue) {
        return externAddEntry(self, java:fromString(headerName), java:fromString(headerValue));
    }

    # Removes a transport header from the request.
    #
    # + headerName - The header name.
    public function remove(string headerName) {
        return externRemove(self, java:fromString(headerName));
    }

    # Removes all transport headers from the message.
    public function removeAll() {
        return externRemoveAll(self);
    }
};

function externExists(Headers headerValues, handle headerName) returns boolean =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;

function externGet(Headers headerValues, handle headerName) returns handle? =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;

function externGetAll(Headers headerValues, handle headerName) returns string[] =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;

function externSetEntry(Headers headerValues, handle headerName, handle headerValue) =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;

function externAddEntry(Headers headerValues, handle headerName, handle headerValue) =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;

function externRemove(Headers headerValues, handle headerName) =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;

function externRemoveAll(Headers headerValues) =
@java:Method {
    class: "org.ballerinalang.net.grpc.nativeimpl.headers.FunctionUtils"
} external;
