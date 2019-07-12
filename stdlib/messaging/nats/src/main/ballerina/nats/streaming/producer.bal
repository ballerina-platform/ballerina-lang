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

    public function __init(Connection conn, string clientId, string clusterId, StreamingConfig? streamingConfig = ()) {
        self.createStreamingConnection(conn, clusterId, clientId, streamingConfig);
    }

    function createStreamingConnection(Connection conn, string clusterId, string clientId,
    StreamingConfig? streamingConfig) = external;

    # Publishes data to a given subject.
    #
    # + subject - NATS subject to publish data 
    # + data - data to publish
    # + return - `string` value representing the NUID (NATS Unique Identifier) of the published message, if the
    #           message gets successfully published and acknowedged by the NATS server OR
    #           `nats/NatsError` with `nuid` and `message` fields in case an error occurs in publishing, the timeout
    #           elapses while waiting for the acknowledgement OR
    #           `nats/NatsError` only with the `message` field in case an error occurrs even before publishing
    #           is completed
    public remote function publish(@untainted string subject, @untainted ContentType data) returns string | NatsError {
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareNatsError("Error in data conversion", err = converted);
        } else {
            return self.externPublish(subject, converted);
        }
    }

    function externPublish(string subject, string | byte[] data) returns string | NatsError = external;

};
