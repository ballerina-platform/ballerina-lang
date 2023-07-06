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

type s1 record {
    float x;
};

function getGlobalVars() returns float {
    s1 v = {};
    float f = v.foo:x;
    return f;
}

listener Listener uninitializedModuleListener;

public listener Listener uninitializedPublicModuleListener;

public class Listener {

    *Listener;

    public function init() {

    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? { }

    public function detach(service object {} s) returns error? { }

    public function 'start() returns error? {

    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }
}
