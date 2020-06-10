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

import ballerina/java;

# The streaming producer provides the capability to publish messages to the NATS streaming server.
# The `nats:StreamingProducer` needs the `nats:Connection` to be initialized.
public type StreamingProducer client object {
    private Connection? conn;

    # Creates a new `nats:StreamingProducer` instance.
    #
    # + connection - An established NATS connection
    # + clientId - A unique identifier of the client
    # + clusterId - The unique identifier of the cluster configured in the NATS server
    # + streamingConfig - The configuration related to the NATS streaming connectivity
    public function __init(Connection connection, public string? clientId = (), public string clusterId = "test-cluster",
    public StreamingConfig? streamingConfig = ()) {
        self.conn = connection;
        streamingProducerInit(self, connection, clusterId, clientId, streamingConfig);
    }

    # Publishes data to a given subject.
    # ```ballerina string|error result = producer->publish(subject, <@untainted>message);```
    #
    # + subject - The subject to send the message 
    # + data - Data to publish
    # + return - The `string` value representing the NUID (NATS Unique Identifier) of the published message if the
    #            message gets successfully published and acknowledged by the NATS server,
    #            a `nats:Error` with NUID and `message` fields in case an error occurs in publishing, the timeout
    #            elapses while waiting for the acknowledgement, or else
    #            a `nats:Error` only with the `message` field in case an error occurs even before publishing
    #            is completed
    public remote function publish(string subject,@untainted Content data) returns string|Error {
        Connection? natsConnection = self.conn;
        if (natsConnection is ()) {
            return NatsError("NATS Streaming Client has been closed.");
        } else {
            string|byte[]|error converted = convertData(data);
            if (converted is error) {
                return prepareError("Error in data conversion", err = converted);
            } else {
                return externStreamingPublish(self, subject, converted, natsConnection);
            }
        }
    }

    # Close the producer.
    #
    # + return - `()` or else a `nats:Error` if unable to complete the close operation.
    public function close() returns error? {
        Connection? natsConnection = self.conn;
        if (natsConnection is Connection) {
            self.conn = ();
            return streamingProducerClose(self, natsConnection);
        }
    }
};

function streamingProducerInit(StreamingProducer streamingClient, Connection conn,
    string clusterId, string? clientId, StreamingConfig? streamingConfig) =
@java:Method {
    class: "org.ballerinalang.nats.streaming.producer.Init"
} external;

function streamingProducerClose(StreamingProducer streamingClient, Connection natsConnection) returns error? =
@java:Method {
    class: "org.ballerinalang.nats.streaming.producer.Close"
} external;

function externStreamingPublish(StreamingProducer producer, string subject, string|byte[] data,
    Connection connection) returns string|Error =
@java:Method {
    class: "org.ballerinalang.nats.streaming.producer.Publish"
} external;
