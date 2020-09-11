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

import ballerina/http;

listener http:Listener lis = new http:Listener(port);
string b7a = "b7a";

int port = o.p;

Obj o = new();
string str = "";
int p = port;

service hello on lis {

    function init() {
        str = b7a;
    }

    resource function sayHello(http:Caller caller, http:Request request) {

        http:Response response = new;
        response.setTextPayload(str);
        checkpanic caller->respond(response);
    }
}

class Obj {
    string str;
    int p = port;

    function init() {
        self.str = b7a;
    }
}
