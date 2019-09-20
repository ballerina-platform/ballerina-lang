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

# NATS `Producer` would act as a basic client allowing to publish messages to the NATS server.
# Producer needs the NATS `Connection` to be initialized.
public type Producer client object {

    private Connection? conn = ();

    # Creates a new NATS `Producer`.
    #
    # + connection - An already-established connection.
    public function __init(Connection connection) {
        self.conn = connection;
        self.init(connection);
    }

    private function init(Connection c) = external;

    # Produces a message to a NATS basic server for the given subject.
    #
    # + subject - The subject to send the message to.
    # + data - Data to publish.
    # + return -  A specific error if there is a problem when publishing the message. Returns () otherwise.
    public remote function publish(string subject, @untainted Content data) returns Error? {
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareError("Error in data conversion", err = converted);
        } else {
            return self.externPublish(subject, converted);
        }
    }

    function externPublish(string subject, string | byte[] data, string? replyTo = ()) returns Error? = external;

    # Produces a message and would wait for a response.
    #
    # + subject - Would represent the topic/queue name.
    # + data - The message body to publish.
    # + duration - The time to wait for a response measured in milliseconds.
    # + return -  The response message or an error.
    public remote function request(string subject, @untainted Content data, int? duration = ()) returns Message|Error {
        string | byte[] | error converted = convertData(data);
        if (converted is error) {
            return prepareError("Error in data conversion", converted);
        } else {
            return self.externRequest(subject, converted, duration);
        }
    }

    function externRequest(string subject, Content data, int? duration = ()) returns Message|Error = external;

    # Close a given connection.
    #
    # + return - Retruns () or the error if unable to complete the close operation.
    public function close() returns Error? {
        self.closeConnection();
        if (self.conn is Connection) {
            self.conn = ();
            log:printInfo("Close the logical connection between producer and connection.");
        }

    }

    private function closeConnection() = external;
};
