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

# Representation of outbound authentication handler for HTTP traffic.
public type OutboundAuthHandler abstract object {

    # Prepare the request with the relevant authentication requirments.
    #
    # + req - `Request` instance
    # + return - Updated `Request` instance or `error` in case of errors
    public function prepare(Request req) returns Request|error;

    # Inspect the request, response and evaluate what to be done.
    #
    # req - `Request` instance
    # resp - `Response` instance
    # + return - Updated `Request` instance or `error` in case of errors or `()` if nothing to be done
    public function inspect(Request req, Response resp) returns Request|error?;
};
