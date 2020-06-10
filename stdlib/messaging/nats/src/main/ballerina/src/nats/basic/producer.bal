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

import ballerina/log;
import ballerina/java;

# The producer provides the capability to publish messages to the NATS server.
# The `nats:Producer` needs the `nats:Connection` to be initialized.
public type Producer client object {

    private Connection? conn = ();

    # Creates a new `nats:Producer`.
    #
    # + connection - An established NATS connection
    public function __init(Connection connection) {
        self.conn = connection;
        producerInit(connection);
    }

    # Publishes data to a given subject.
    # ```ballerina
    # nats:Error? result = producer->publish(subject, <@untainted>message);
    # ```
    #
    # + subject - The subject to send the message
    # + data - Data to publish
    # + replyTo - The subject or the callback service to which the receiver should send the response
    # + return -  `()` or else a `nats:Error` if there is a problem when publishing the message
    public remote function publish(string subject, @untainted Content data, (string | service)? replyTo = ())
                    returns Error? {
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareError("Error in data conversion", err = converted);
        } else {
            return externPublish(self, subject, converted, replyTo);
        }
    }

    # Publishes data to a given subject and waits for a response.
    # ```ballerina
    # nats:Message|nats:Error reqReply = producer->request(subject, <@untainted>message, 5000);
    # ```
    #
    # + subject - The subject to send the message
    # + data - Data to publish
    # + duration - The time (in milliseconds) to wait for the response
    # + return -  The `nats:Message` response or else a `nats:Error` if an error is encountered
    public remote function request(string subject, @untainted Content data, int? duration = ()) returns Message|Error {
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareError("Error in data conversion", converted);
        } else {
            return externRequest(self, subject, converted, duration);
        }
    }

    # Closes a given connection.
    #
    # + return - `()` or else a `nats:Error` if unable to complete the close the operation
    public function close() returns Error? {
        closeConnection(self);
        if (self.conn is Connection) {
            self.conn = ();
            log:printInfo("Close the logical connection between producer and connection.");
        }

    }
};

function producerInit(Connection c) =
@java:Method {
    class: "org.ballerinalang.nats.basic.producer.Init"
} external;

function closeConnection(Producer producer) =
@java:Method {
    class: "org.ballerinalang.nats.basic.producer.CloseConnection"
} external;

function externRequest(Producer producer, string subject, Content data, int? duration = ()) returns Message | Error =
@java:Method {
    class: "org.ballerinalang.nats.basic.producer.Request"
} external;

function externPublish(Producer producer, string subject, string | byte[] data, (string | service)? replyTo = ())
returns Error? = @java:Method {
    class: "org.ballerinalang.nats.basic.producer.Publish"
} external;
