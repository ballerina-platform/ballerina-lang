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

import ballerina/lang.'object as lang;
import ballerina/java;

# Represents the NATS streaming server connection to which a subscription service should be bound in order to
# receive messages of the corresponding subscription.
public type StreamingListener object {

    *lang:Listener;

    private Connection connection;
    private string clusterId;
    private string? clientId;
    private StreamingConfig? streamingConfig;

    # Creates a new Streaming Listener.
    #
    # + connection - An established NATS connection
    # + clusterId - The unique identifier of the cluster configured in the NATS server. The default value is `test-cluster`
    # + clientId - The unique identifier of the client. The `clientId` should be unique across all the subscriptions.
    #              Therefore, multilpe subscription services cannot be bound to a single listener
    # + streamingConfig - The configuration related to the NATS streaming connectivity
    public function __init(Connection connection, public string? clientId = (), public string clusterId = "test-cluster",
    public StreamingConfig? streamingConfig = ()) {
        self.connection = connection;
        self.clusterId = clusterId;
        self.clientId = clientId;
        self.streamingConfig = streamingConfig;
        streamingListenerInit(self);
    }

    # Binds a service to the `nats:StreamingListener`.
    #
    # + s - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else a `nats:Error` upon failure to register the listener
    public function __attach(service s, string? name = ()) returns error? {
        streamingAttach(self, s, self.connection);
    }

    # Stops consuming messages and detaches the service from the `nats:StreamingListener`.
    #
    # + s - Type descriptor of the service
    # + return - `()` or else a `nats:Error` upon failure to detach the service
    public function __detach(service s) returns error? {
        streamingDetach(self, s);
    }

    # Starts the `nats:StreamingListener`.
    #
    # + return - `()` or else a `nats:Error` upon failure to start the listener
    public function __start() returns error? {
         streamingSubscribe(self, self.connection, self.clusterId, self.clientId, self.streamingConfig);
    }

    # Stops the `nats:StreamingListener` gracefully.
    #
    # + return - `()` or else a `nats:Error` upon failure to stop the listener
    public function __gracefulStop() returns error? {
        return ();
    }

    # Stops the `nats:StreamingListener` forcefully.
    #
    # + return - `()` or else a `nats:Error` upon failure to stop the listener
    public function __immediateStop() returns error? {
        return self.close();
    }

    function close() returns error? {
        return streamingListenerClose(self, self.connection);
    }
};

function streamingListenerInit(StreamingListener lis) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Init"
} external;

function streamingSubscribe(StreamingListener streamingClient, Connection conn,
                            string clusterId, string? clientId, StreamingConfig? streamingConfig) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Subscribe"
} external;

function streamingAttach(StreamingListener lis, service serviceType, Connection conn) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Attach"
} external;

function streamingDetach(StreamingListener lis, service serviceType) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Detach"
} external;

function streamingListenerClose(StreamingListener lis,  Connection natsConnection) returns error? =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Close"
} external;
