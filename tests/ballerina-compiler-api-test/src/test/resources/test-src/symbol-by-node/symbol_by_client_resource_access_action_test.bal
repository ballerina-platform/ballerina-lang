// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

public client class Client {
    public string url;

    public function init(string url) {
        self.url = url;
    }

    resource function get path2/[string id]/path3(string str) returns string {
        return "";
    }

    resource function post path2/[string id]/path4(string str, int a) returns string {
        return "";
    }
}

public function test() {
    Client cl = new ("");
    cl-> /path2/[""]/path4.post();
}
