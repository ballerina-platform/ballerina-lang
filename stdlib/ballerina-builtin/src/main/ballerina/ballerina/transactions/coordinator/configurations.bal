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

import ballerina.config;

const string basePath = "/balcoordinator";
const string initiatorCoordinatorBasePath = basePath + "/initiator";
const string initiator2pcCoordinatorBasePath = basePath + "/initiator/2pc";
const string participant2pcCoordinatorBasePath = basePath + "/participant/2pc";
const string registrationPath = "/register";
const string registrationPathPattern = "/{transactionBlockId}" + registrationPath;

const string coordinatorHost = getCoordinatorHost();
const int coordinatorPort = getCoordinatorPort();

function getCoordinatorHost () returns (string host) {
    host = config:getInstanceValue("http", "coordinator.host");
    if (host == null || host == "") {
        host = getHostAddress();
    }
    return;
}

function getCoordinatorPort () returns (int port) {
    var p, e = <int>config:getInstanceValue("http", "coordinator.port");
    if (e != null) {
        port = getAvailablePort();
    } else {
        port = p;
    }
    return;
}

endpoint http:ServiceEndpoint coordinatorServerEP {
    host:coordinatorHost,
    port:coordinatorPort
};
