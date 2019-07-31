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

# Represent the listener shape that is used to provide values to services.
public type AbstractListener abstract object {

    # Handle listner start.
    # + return - error when fail to start, nil otherwise.
    public function __start() returns error?;

    # Handle listner stop.
    # + return - error when fail to stop, nil otherwise.
    public function __stop() returns error?;

    # Handle attaching to service `s`.
    # + return - error if attachment failes, nil othrwise.
    public function __attach(service s, string? name = ()) returns error?;
};
