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

# NATS basic subscription parameters.
#
# + subject - Name of the subject.
# + queueName - Name of the queue group.
# + pendingLimits - Parameters to set limits on the maximum number of pending messages,
#                               or maximum size of pending messages.
public type SubscriptionConfigData record {|
    string subject;
    string queueName?;
    PendingLimits pendingLimits?;
|};

# Parameters to set limits on the maximum number of messages, or maximum size of messages this consumer will
# hold before it starts to drop new messages waiting for the resource functions to drain the queue.
# Setting a value to anything less than or equal to 0 will disable this check.
#
# + maxMessages - Maximum number of pending messages retrieved and held by the consumer service,
#                   defaults to 65536.
# + maxBytes - Total size of pending messages in bytes retrieved and held by the consumer service,
#               defaults to 67108864.
public type PendingLimits record {|
    int maxMessages;
    int maxBytes;
|};

# Basic Subscription config annotation.
public annotation SubscriptionConfigData SubscriptionConfig on service;
