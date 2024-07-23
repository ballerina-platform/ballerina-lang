// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public isolated client class Client {
    final string greeting;

    public isolated function init(string greeting = "Hello World!") returns error? {
        self.greeting = greeting;
        return;
    }

    resource isolated function get users\.getPresence(map<string|string[]> headers = {}) returns string|error {
        return string `/users.getPresence ${self.greeting} ${headers.toString()}`;
    }
}
