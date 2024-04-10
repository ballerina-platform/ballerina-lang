// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
//
//  WSO2 LLC. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing,
//  software distributed under the License is distributed on an
//  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  KIND, either express or implied.  See the License for the
//  specific language governing permissions and limitations
//  under the License.
//

service / on new Listener() {

    resource function post h2(string[] arr) {
        _ = start callFunction(arr.clone());
    }

    resource function post h3(string[] arr) {
        string[] newArray = [...arr];
        _ = start callFunction(newArray.clone());
    }

    resource function get h4() {
        _ = start fn();
    }
}

function fn(any x = ()) returns any {
    _ = start fn(fn());
}

isolated function callFunction(string[] arr) {
}

class Listener {
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
