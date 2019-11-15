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

import ballerinax/java;

# NATS `StreamingProducer` would act as a client allowing to publish messages to the
# NATS streaming server. `StreamingProducer` needs the NATS `Connection` to be initialized.
public type StreamingProducer client object {
    private Connection? conn;

    # Creates a new NATS `StreamingProducer`.
    #
    # + connection - An established NATS connection.
    # + clientId - A unique identifier representing the client.
    # + clusterId - The ID of the cluster configured in the NATS server.
    # + streamingConfig - The configuration related to the NATS streaming connectivity.
    public function __init(Connection connection, public string? clientId = (), public string clusterId = "test-cluster",
    public StreamingConfig? streamingConfig = ()) {
        self.conn = connection;
        createStreamingConnection(self, connection, java:fromString(clusterId), clientId, streamingConfig);
    }

    # Publishes data to a given subject.
    #
    # + subject - The subject to send the message to.
    # + data - Data to publish.
    # + return - `string` value representing the NUID (NATS Unique Identifier) of the published message, if the
    #           message gets successfully published and acknowledged by the NATS server OR
    #           `nats/Error` with NUID and `message` fields in case an error occurs in publishing, the timeout
    #           elapses while waiting for the acknowledgement OR
    #           `nats/Error` only with the `message` field in case an error occurs even before publishing
    #           is completed
    public remote function publish(string subject, @untainted Content data) returns string | Error {
        if (self.conn is ()) {
            return Error(message = "NATS Streaming Client has been closed.");
        }
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareError("Error in data conversion", err = converted);
        } else {
            return externStreamingPublish(self, java:fromString(subject), converted);
        }
    }

    # Close the producer.
    #
    # + return - Returns () or the error if unable to complete the close operation.
    public function close() returns error? {
        if (self.conn is Connection) {
            Connection? natsConnection = self.conn;
            self.conn = ();
            return detachFromNatsConnection(self, natsConnection);
        }
    }
};

function detachFromNatsConnection(StreamingProducer|StreamingListener streamingClient, Connection? natsConnection)
returns error? =
@java:Method {
    class: "org.ballerinalang.nats.streaming.producer.CloseConnection"
} external;

function createStreamingConnection(StreamingProducer|StreamingListener streamingClient, Connection? conn,
handle clusterId, string? clientId, StreamingConfig? streamingConfig) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.producer.CreateStreamingConnection"
} external;

function externStreamingPublish(StreamingProducer producer, handle subject, string | byte[] data) returns string | Error =
@java:Method {
    class: "org.ballerinalang.nats.streaming.producer.Publish"
} external;
