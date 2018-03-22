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

package ballerina.transactions.coordinator;

import ballerina/config;

const string basePath = "/balcoordinator";
const string initiatorCoordinatorBasePath = basePath + "/initiator";
const string initiator2pcCoordinatorBasePath = basePath + "/initiator/2pc";
const string participant2pcCoordinatorBasePath = basePath + "/participant/2pc";
const string registrationPath = "/register";
const string registrationPathPattern = "/{transactionBlockId}" + registrationPath;

const string coordinatorHost = getCoordinatorHost();
const int coordinatorPort = getCoordinatorPort();

function getCoordinatorHost () returns string {
    string host;
    var result = config:getAsString("http.coordinator.host");
    match result {
        string h => host = h;
        any|null => host = getHostAddress();
    }
    return host;
}

function getCoordinatorPort () returns int {
    int port;
    var result = config:getAsString("http.coordinator.port");
    match result {
        string p => {
            var result2 = <int>p;
            match result2 {
                error e => port = getAvailablePort();
                int p2 => port = p2;
            }
        }
        any|null => port = getAvailablePort();
    }
    return port;
}

endpoint http:ServiceEndpoint coordinatorServerEP {
    host:coordinatorHost,
    port:coordinatorPort
};