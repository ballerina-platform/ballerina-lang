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
import ballerinax/java;

# Represents the NATS Streaming Server connection, to which a subscription service should be bound to in order to receive messages
# of the corresponding subscription.
public type StreamingListener object {

    *lang:Listener;

    private Connection? connection;
    private string clusterId;
    private string? clientId;
    private StreamingConfig? streamingConfig;

    # Creates a new StreamingListener.
    #
    # + connection - An established NATS connection.
    # + clusterId - The ID of the cluster configured in the NATS server. Default value is `test-cluster`.
    # + clientId - A unique identifier representing the client. The `clientId` should be unique across subscriptions. Therefore,
    #              multilpe subscription services cannot be bound to a single listener.
    # + streamingConfig - The configuration related to the NATS streaming connectivity.
    public function __init(Connection connection, public string? clientId = (), public string clusterId = "test-cluster",
    public StreamingConfig? streamingConfig = ()) {
        self.connection = connection;
        self.clusterId = clusterId;
        self.clientId = clientId;
        self.streamingConfig = streamingConfig;
        streamingListenerInit(self);
    }

    public function __attach(service s, string? name = ()) returns error? {
        streamingAttach(self, s, self.connection);
    }

    public function __detach(service s) returns error? {
        streamingDetach(self, s);
    }

    public function __start() returns error? {
         createStreamingConnection(self, self.connection, java:fromString(self.clusterId), self.clientId, self.streamingConfig);
         streamingSubscribe(self);
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return self.close();
    }

    function close() returns error? {
        if (self.connection is Connection) {
            Connection? natsConnection = self.connection;
            self.connection = ();
            return detachFromNatsConnection(self, natsConnection);
        }
    }
};

function streamingListenerInit(StreamingListener lis) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Init"
} external;

function streamingSubscribe(StreamingListener lis) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Subscribe"
} external;

function streamingAttach(StreamingListener lis, service serviceType, Connection? conn) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Attach"
} external;

function streamingDetach(StreamingListener lis, service serviceType) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.consumer.Detach"
} external;
