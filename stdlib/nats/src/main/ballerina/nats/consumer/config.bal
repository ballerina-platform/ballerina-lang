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

# Represents the list of paramters required to create a subscription.
#
# + subject -  Name of the subject.
# + manualAck - True if the acknowledgments will be done manually.
# + ackWait -  Amount of time (in seconds) the client should wait before retrying.
# + startSeq - Sequence id of the message which should be consumed.
# + durableName - Unique name to identify the durable subscription.
public type ConsumerConfigData record {|
    string subject;
    boolean manualAck = false;
    int ackWait?;
    int startSeq?;
    string durableName?;
|};

# Service descriptor data generated at compile time. This is for internal use.
public annotation ConsumerConfigData ConsumerConfig on service;
