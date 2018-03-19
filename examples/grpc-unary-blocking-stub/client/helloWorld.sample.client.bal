// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package client;
import ballerina.io;

function main (string[] args) {
    endpoint helloWorldBlockingClient helloWorldBlockingEp {
            host: "localhost",
            port: 9090
        };

    var res, err = helloWorldBlockingEp -> hello("WSO2");
    if (err != null) {
        io:println("Error from Connector: " + err.message);
    } else {
        io:println("Client Got Response : ");
        io:println(res);
    }
}


