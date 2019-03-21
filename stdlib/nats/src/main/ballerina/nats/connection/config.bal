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
# + host - The remote host name/IP
# + port - The remote port
# + clusterId - The name of the NATS server cluster the connection should be established
# + clientId - Unique identifier for the client
# + connectionTimeout - Specify the time server should wait to establish a connection (in seconds)
# + maxPubAcksInFlight - Specify the number of messages which can be dispatched without receiving acks
# + ackTimeout - The time (in seconds) before retrying to send an unacknowledged message
public type ConnectionConfig record {
    string host;
    int port;
    string clusterId = "test-cluster";
    string clientId = "ballerina_client";
    int connectionTimeout = 30;
    int maxPubAcksInFlight = 100;
    int ackTimeout = 30;
    !...;
};
