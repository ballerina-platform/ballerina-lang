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

package ballerina.transactions;

import ballerina/config;

@final string basePath = "/balcoordinator";
@final string initiatorCoordinatorBasePath = basePath + "/initiator";
@final string initiator2pcCoordinatorBasePath = basePath + "/initiator/2pc";
@final string participant2pcCoordinatorBasePath = basePath + "/participant/2pc";
@final string registrationPath = "/register";
@final string registrationPathPattern = "/{transactionBlockId}" + registrationPath;

@final string coordinatorHost = config:getAsString("http.coordinator.host", default = getHostAddress());
@final int coordinatorPort = config:getAsInt("http.coordinator.port", default = getAvailablePort());

endpoint http:Listener coordinatorListener {
    host:coordinatorHost,
    port:coordinatorPort
};