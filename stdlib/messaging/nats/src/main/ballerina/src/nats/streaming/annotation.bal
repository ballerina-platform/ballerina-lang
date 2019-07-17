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

#  NATS Streaming Subscription Parameters.
#
# + subject - Name of the subject to which it is subscribed 
# + queueName - The name of the queue group to which the subscription belongs 
# + durableName - If set, this will survive client restarts
# + maxInFlight - The number of messages the cluster will have inflight without an ACK
# + ackWaitInSeconds - The time the cluster will wait for an ACK for a given message (in Seconds)
# + subscriptionTimeoutInSeconds - The time (in Seconds) the subscription will wait if a network failure occurs during the creation of it.
# + manualAck - Do manual ACKs
# + startPosition - The position to start receiving messages 
public type StreamingSubscriptionConfigData record {|
   string subject;
   string queueName?;
   string durableName?;
   int maxInFlight = 1024;
   int ackWaitInSeconds = 30;
   int subscriptionTimeoutInSeconds = 2;
   boolean manualAck = false;
   StartPosition startPosition = NEW_ONLY;
|};

# Streaming subscription configuration annotation.
public annotation StreamingSubscriptionConfigData StreamingSubscriptionConfig on service;

public const NEW_ONLY = "NEW_ONLY";
public const LAST_RECEIVED = "LAST_RECEIVED";
public const FIRST = "FIRST";
public const TIME_DELTA_START = "TIME_DELTA_START";
public const SEQUENCE_NUMBER = "SEQUENCE_NUMBER";

public type TimeDeltaStart [TIME_DELTA_START, int];

public type SequenceNumber [SEQUENCE_NUMBER, int];

# The position to start receiving messages.
# NEW_ONLY - Specifies that message delivery should start with the messages, which are published after the subscription
# is created.
# LAST_RECEIVED - Specifies that message delivery should start with the last (most recent) message stored for
# this subject.
# TimeDeltaStart - Specifies that message delivery should start with a given historical time delta (from now).
# SequenceNumber - Specifies that message delivery should start at the given sequence number.
# FIRST - Specifies that message delivery should begin at the oldest available message for this subject.
public type StartPosition NEW_ONLY | LAST_RECEIVED | TimeDeltaStart | SequenceNumber | FIRST;
