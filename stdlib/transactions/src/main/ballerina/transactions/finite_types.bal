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

type TransactionState TXN_STATE_ACTIVE|TXN_STATE_PREPARED|TXN_STATE_COMMITTED|TXN_STATE_ABORTED;
const TXN_STATE_ACTIVE = "active";
const TXN_STATE_PREPARED = "prepared";
const TXN_STATE_COMMITTED = "committed";
const TXN_STATE_ABORTED = "aborted";

type PrepareResult PREPARE_RESULT_PREPARED|PREPARE_RESULT_ABORTED|PREPARE_RESULT_COMMITTED|PREPARE_RESULT_READ_ONLY;
const PREPARE_RESULT_PREPARED = "prepared";
const PREPARE_RESULT_ABORTED = "aborted";
const PREPARE_RESULT_COMMITTED = "committed";
const PREPARE_RESULT_READ_ONLY = "read-only";

type NotifyResult NOTIFY_RESULT_COMMITTED|NOTIFY_RESULT_ABORTED;
const NOTIFY_RESULT_COMMITTED = "committed";
const NOTIFY_RESULT_ABORTED = "aborted";

type PrepareDecision PREPARE_DECISION_COMMIT|PREPARE_DECISION_ABORT;
const PREPARE_DECISION_COMMIT = "commit";
const PREPARE_DECISION_ABORT = "abort";
