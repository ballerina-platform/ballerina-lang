// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/serv_classes.A;

public service class Service {
    remote function foo() returns int {
        return 22;
    }

    resource function get bar() returns string {
        return "bar";
    }

    resource function put bar() returns string {
        return "put-bar";
    }

    resource function get foo/bar(int i, string j) returns string {
        return i.toString() + j;
    }

    remote function getRemoteCounter(int num, decimal value, string msg = "test message") {
    }

    resource function get [A:T t]() returns string {
        return t;
    }
}
