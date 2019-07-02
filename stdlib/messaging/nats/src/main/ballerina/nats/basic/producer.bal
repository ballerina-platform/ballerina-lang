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

# NATS producer would act as a basic client allowing to publish messages to NATS server.
# Producer need NATS connection to initialize.
public type Producer client object {

    private Connection? connection = ();

    # Creates a new producer.
    #
    # + c - An already established connection.
    public function __init(Connection c) {
        self.connection = c;
    }

    # Produces a message to a NATS basic server for the given subject.
    #
    # + subject - Could also be referred as the 'topic/queue' name.
    # + replyTo - Subject for the receiver to reply. Optional parameter. Set only if reply is needed.
    # + message - Message could be byte[] representation.
    # + return -  A specific error, if there is a problem when publishing the message. () otherwise.
    public remote function publish(string subject, byte[] message, string? replyTo = ()) returns error? = external;

    # Produces a message and would wait for a response.
    #
    # + subject - Would represent the topic/queue name.
    # + message - Message could be byte[] representation.
    # + duration - the time to wait for a response. measure in milliseconds
    # + return -  Response message or an error.
    public remote function request(string subject, byte[] message, int? duration) returns Message|error = external;

    # Close a given connection.
    #
    # + return - () or error if unable to complete close operation.
    public function close() returns error? {
        if (self.connection is Connection) {
            self.connection = ();
            log:printInfo("Close the logical connection between producer and connection.");
        }
    }
};
