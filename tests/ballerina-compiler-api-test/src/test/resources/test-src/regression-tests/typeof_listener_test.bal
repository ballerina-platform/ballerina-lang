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

service Service on new Listener({}) {
    remote function onMessage(string s) returns int {
        return 0;
    }
}

public type Config record {|
    int field1;
    int field2;
|};

public type Service service object {
    remote function onMessage(string s) returns int;
};

public class Listener {
    function init(Config config) {}

    public function attach(Service s, string|string[]? name = ()) returns error? {}

    public function detach(Service s) returns error? {}

    public function 'start() returns error? {}

    public function gracefulStop() returns error? {}

    public function immediateStop() returns error? {}
}
