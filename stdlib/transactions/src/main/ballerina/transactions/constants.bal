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

import ballerina/config;

@final string basePath = "/balcoordinator";
@final string initiatorCoordinatorBasePath = basePath + "/initiator";
@final string initiator2pcCoordinatorBasePath = basePath + "/initiator/2pc";
@final string participant2pcCoordinatorBasePath = basePath + "/participant/2pc";
@final string registrationPath = "/register";
@final string registrationPathPattern = "/{transactionBlockId}" + registrationPath;

@final string coordinatorHost = config:getAsString("b7a.transactions.coordinator.host", default = getHostAddress());
@final int coordinatorPort = config:getAsInt("b7a.transactions.coordinator.port", default = getAvailablePort());

@final string TRANSACTION_CONTEXT_VERSION = "1.0";

@final public string COMMAND_PREPARE = "prepare";
@final public string COMMAND_COMMIT = "commit";
@final public string COMMAND_ABORT = "abort";

@final public string PREPARE_RESULT_PREPARED_STR = "prepared";
@final public string PREPARE_RESULT_ABORTED_STR = "aborted";
@final public string PREPARE_RESULT_COMMITTED_STR = "committed";
@final public string PREPARE_RESULT_READ_ONLY_STR = "read-only";
@final string PREPARE_RESULT_FAILED_STR = "failed";

@final public string NOTIFY_RESULT_NOT_PREPARED_STR = "not-prepared";
@final public string NOTIFY_RESULT_FAILED_EOT_STR = "failed-eot";

@final public string NOTIFY_RESULT_COMMITTED_STR = "committed";
@final public string NOTIFY_RESULT_ABORTED_STR = "aborted";

@final public string OUTCOME_COMMITTED = "committed";
@final public string OUTCOME_ABORTED = "aborted";
@final string OUTCOME_MIXED = "mixed";
@final string OUTCOME_HAZARD = "hazard";

@final public string TRANSACTION_UNKNOWN = "Transaction-Unknown";

endpoint http:Listener coordinatorListener {
    host:coordinatorHost,
    port:coordinatorPort
};
