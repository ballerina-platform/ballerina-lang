// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import ballerina/lang.runtime;

public class Listener {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public function main() {
    Listener l = new("TestListener");
    runtime:registerListener(l);
}

public type MutualSslHandshake record {|
    MutualSslStatus status = PASSED;
    string? base64EncodedCert = ();
|};

public type MutualSslStatus PASSED|FAILED|();
public const PASSED = "passed";
public const FAILED = "failed";
