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

# Error type specific to the `task:Scheduler` object functions.
public type SchedulerError distinct error;

# Error type specific to the `task:Listener` object functions.
public type ListenerError distinct error;

# Represents the Union error type of the ballerina/task module. This error type represents any error that can occur during the
# execution of task APIs.
public type Error SchedulerError|ListenerError;
