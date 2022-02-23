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

listener Listener lis = new Listener(port);
string b7a = "b7a";

int port = o.p;

Obj o = new();
string str = "";
int p = port;

service /hello on new Listener(8080) {

    function init() {
        str = b7a;
    }

    resource function get hello() {

        Response response = new;
        response.setTextPayload(str);
    }
}

class Obj {
    string str;
    int p = port;

    function init() {
        self.str = b7a;
    }
}

class Response {
    string text = "";

    function setTextPayload(string str) {
        self.text = str;
    }
}

class Listener {
    public function init(int port) {
    }

    public function attach(service object {} s, string|string[]? name = ()) returns error? {
        return;
    }

    public function detach(service object {} s) returns error? {
        return;
    }

    public function 'start() returns error? {
        return;
    }

    public function gracefulStop() returns error? {
        return;
    }

    public function immediateStop() returns error? {
        return;
    }
}
