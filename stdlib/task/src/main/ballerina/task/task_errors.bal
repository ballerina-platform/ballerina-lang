// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Record type to hold details of task errors.
#
# + message - Specific error message of the error
# + cause - If there is another error which caused this particular error, the causing error will be stored here
public type Detail record {
    string message;
    error cause?;
};

public const SCHEDULER_ERROR_REASON = "{ballerina/task}SchedulerError";

# Errors occurred in task:Scheduler functions.
public type SchedulerError error<SCHEDULER_ERROR_REASON, Detail>;

public const LISTENER_ERROR_REASON = "{ballerina/task}ListenerError";

# Errors occurred in task:Listener functions.
public type ListenerError error<LISTENER_ERROR_REASON, Detail>;

# Union error type of ballerina/task module.
public type TaskError SchedulerError|ListenerError;
