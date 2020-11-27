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

# Represents the shape expected from all listeners.
public type Listener object {
    # Handle service attachment to the listener.
    #d
    # + s - the service to attach
    # + name - optionally a name associated with the service
    # + return - `()` if no error occurred, and an error otherwise
    public function __attach(service s, string? name = ()) returns error?;

    # Handle service detachment from the listener.
    #
    # + s - the service to detach
    # + return - `()` if no error occurred, and an error otherwise

    public function __detach(service s) returns error?;
    # Handle listener start.
    #
    # + return - `()` if no error occurred, and an error otherwise
    public function __start() returns error?;

    # Handle listener graceful stop.
    #
    # + return - `()` if no error occurred, and an error otherwise
    public function __gracefulStop() returns error?;

    # Handle listener immediate stop.
    #
    # + return - `()` if no error occurred, and an error otherwise
    public function __immediateStop() returns error?;
};
