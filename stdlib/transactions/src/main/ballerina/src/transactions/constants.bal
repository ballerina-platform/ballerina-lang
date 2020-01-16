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
import ballerina/http;

const string basePath = "/balcoordinator";
final string initiatorCoordinatorBasePath = basePath + "/initiator";
final string initiator2pcCoordinatorBasePath = basePath + "/initiator/2pc";
final string participant2pcCoordinatorBasePath = basePath + "/participant/2pc";
final string registrationPath = "/register";
final string registrationPathPattern = "/{transactionBlockId}" + registrationPath;

final string coordinatorHost = config:getAsString("b7a.transactions.coordinator.host", getHostAddress());
final int coordinatorPort = config:getAsInt("b7a.transactions.coordinator.port", getAvailablePort());

const string TRANSACTION_CONTEXT_VERSION = "1.0";

const string COMMAND_PREPARE = "prepare";
const string COMMAND_COMMIT = "commit";
const string COMMAND_ABORT = "abort";

const string PREPARE_RESULT_PREPARED_STR = "prepared";
const string PREPARE_RESULT_ABORTED_STR = "aborted";
const string PREPARE_RESULT_COMMITTED_STR = "committed";
const string PREPARE_RESULT_READ_ONLY_STR = "read-only";
const string PREPARE_RESULT_FAILED_STR = "failed";

const string NOTIFY_RESULT_NOT_PREPARED_STR = "not-prepared";
const string NOTIFY_RESULT_FAILED_EOT_STR = "failed-eot";

const string NOTIFY_RESULT_COMMITTED_STR = "committed";
const string NOTIFY_RESULT_ABORTED_STR = "aborted";

const string OUTCOME_COMMITTED = "committed";
const string OUTCOME_ABORTED = "aborted";
const string OUTCOME_MIXED = "mixed";
const string OUTCOME_HAZARD = "hazard";

const string TRANSACTION_UNKNOWN = "Transaction-Unknown";

listener http:Listener coordinatorListener = new(coordinatorPort, { host: coordinatorHost });
