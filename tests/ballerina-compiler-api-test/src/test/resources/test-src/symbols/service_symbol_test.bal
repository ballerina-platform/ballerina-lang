// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener Listener aListener = new ();

# A listener
@a1
isolated service / on aListener {

    # Name
    @a3
    final string name = "/";

    # ID
    @a3
    final int id = 0;

    # Value
    @a3
    final float value = 0.0f;

    # Get one
    # + name - Parameter
    # + return - Return name  
    @a2
    resource function get one(string name) returns string {
        return "/" + name;
    }

    # Method two
    @a2
    public isolated function two() {
    }

    # Method three
    @a2
    public transactional isolated function three() {
    }

    # Method four
    @a2
    remote function four() {
    }
}

// utils
class Listener {

    public function init() returns error? {
    }

    public function attach(service object {} s, string|string[]? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }
}

public annotation a1 on service;

public annotation a2 on function;

public annotation a3 on field;
