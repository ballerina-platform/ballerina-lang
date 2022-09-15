// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/module1 as mod;
import ballerina/lang.'object as lang;

public listener Listener test = ;

public class Listener {

    *lang:Listener;

    private int port = 0;

    public function 'start() returns error? {
        return self.startEndpoint();
    }

    public function gracefulStop() returns error? {
        return self.gracefulStop();
    }

    public function immediateStop() returns error? {
        error err = error("not implemented");
        return err;
    }

    public function attach(service s, string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function __detach(service s) returns error? {
        return self.detach(s);
    }

    public function init(int port) {
    }

    public function initEndpoint() returns error? {
        return ();
    }

    function register(service s, string? name) returns error? {
        return ();
    }

    function startEndpoint() returns error? {
        return ();
    }

    function gracefulStop() returns error? {
        return ();
    }

    function detach(service s) returns error? {
        return ();
    }
};