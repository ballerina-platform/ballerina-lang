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

# Represents the list of paramters required to establish a connection.
#
# + host - Remote server host name/IP.
# + port - Remote server port.
# + clusterId - Name of the NATS server cluster.
# + clientId - Unique identifier for the client.
# + connectionTimeout - Number of seconds to hold an idle connection.
# + maxPubAcksInFlight - Number of messages which could be dispatched without receiving acks.
# + ackTimeout - Time (in seconds) to wait for an acknowledment before retrying.
public type ConnectionConfig record {|
    string host;
    int port;
    string clusterId = "test-cluster";
    string clientId = "ballerina_client";
    int connectionTimeout = 30;
    int maxPubAcksInFlight = 100;
    int ackTimeout = 30;
|};
