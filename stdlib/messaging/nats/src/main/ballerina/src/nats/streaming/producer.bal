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

public type StreamingProducer client object {
    Connection? connection;

    public function __init(Connection conn, public string? clientId = (), public string clusterId = "test-cluster",
    public StreamingConfig? streamingConfig = ()) {
        self.connection = conn;
        createStreamingConnection(self, conn, clusterId, clientId, streamingConfig);
    }

    # Publishes data to a given subject.
    #
    # + subject - NATS subject to publish data 
    # + data - data to publish
    # + return - `string` value representing the NUID (NATS Unique Identifier) of the published message, if the
    #           message gets successfully published and acknowedged by the NATS server OR
    #           `nats/Error` with `nuid` and `message` fields in case an error occurs in publishing, the timeout
    #           elapses while waiting for the acknowledgement OR
    #           `nats/Error` only with the `message` field in case an error occurrs even before publishing
    #           is completed
    public remote function publish(string subject, @untainted Content data) returns string | Error {
        if (self.connection is ()) {
            Error e = error(message = "NATS Streaming Client has been closed.");
            return e;
        }
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareError("Error in data conversion", err = converted);
        } else {
            return self.externPublish(subject, converted);
        }
    }

    function externPublish(string subject, string | byte[] data) returns string | Error = external;

    # Close the producer.
    #
    # + return - Retruns () or the error if unable to complete the close operation.
    public function close() returns error? {
        if (self.connection is Connection) {
            Connection? natsConnection = self.connection;
            self.connection = ();
            return detachFromNatsConnection(self, natsConnection);
        }
    }
};

function detachFromNatsConnection(StreamingProducer|StreamingListener streamingClient, Connection? natsConnection)
returns error? = external;

function createStreamingConnection(StreamingProducer|StreamingListener streamingClient, Connection? conn,
string clusterId, string? clientId, StreamingConfig? streamingConfig) = external;
