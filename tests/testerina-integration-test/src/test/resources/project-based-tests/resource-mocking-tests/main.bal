// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

Client clientEndpoint = check new ("http://localhost:9090");

public client class Client {

    function init(string url) returns error? {

    }

    resource function get greeting/hello() returns string {
        return "hey";
    }

    resource function get greeting/hello/[string name]() returns string {
        return "hey";
    }

    resource function post greeting/hello() returns string {
        return "hey";
    }

    resource function post greeting/hello/[string name]() returns string {
        return "hey";
    }

    resource function get greeting/hello1(string a) returns string {
        return "hey";
    }

    resource function get greeting/hello2/[string name](string a) returns string {
        return "hey";
    }

    resource function post greeting/hello1(string a) returns string {
        return "hey";
    }

    resource function post greeting/hello2/[string name](string a) returns string {
        return "hey";
    }

    resource function post greeting/hello2/[string name]/[string town](string a, int b) returns string {
        return "hey";
    }

    resource function post greeting/hello2/[int integ]/[decimal decim]/[string str](string a, int b) returns string {
        return "hey";
    }

    resource function get greeting/hello3() {
        return;
    }

    resource function get greeting/hello4() returns error? {
        return;
    }

    remote function alp() returns string {
        return "alp";
    }
}
