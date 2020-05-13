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

# The configurations for the NATS basic subscription.
#
# + subject - Name of the subject
# + queueName - Name of the queue group
# + pendingLimits - Parameters to set limits on the maximum number of pending messages
#                   or maximum size of pending messages
public type SubscriptionConfigData record {|
    string subject;
    string queueName?;
    PendingLimits pendingLimits?;
|};

# The configurations to set limits on the maximum number of messages or maximum size of messages this consumer will
# hold before it starts to drop new messages waiting for the resource functions to drain the queue.
# Setting a value less than or equal to 0 will disable this check.
#
# + maxMessages - Maximum number of pending messages retrieved and held by the consumer service.
#                 The default value is 65536
# + maxBytes - Total size of pending messages in bytes retrieved and held by the consumer service.
#              The default value is 67108864
public type PendingLimits record {|
    int maxMessages;
    int maxBytes;
|};

# The annotation, which is used to configure the basic subscription.
public annotation SubscriptionConfigData SubscriptionConfig on service;
