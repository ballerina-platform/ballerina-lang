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

# Public Ballerina API - Interface to an AMQ `Connection`.
public type Connection object {

    # Initializes a Ballerina RabbitMQ `Connection` object.
    #
    # + connectionConfiguration - Holds connection parameters required to initialize the `Connection`.
    public function __init(ConnectionConfiguration connectionConfiguration) {
        self.createConnection(connectionConfiguration);
    }

    # Closes the RabbitMQ `Connection` and all it's `Channel`s.
    # It waits with a provided timeout for all the close operations to complete.
    # When timeout is reached the socket is forced to close.
    #
    # + closeCode - The close code (For information, go to the "Reply Codes" section in the
    #               [AMQP 0-9-1 specification] (#https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)).
    # + closeMessage - A message indicating the reason for closing the connection.
    # + timeout - Timeout (in milliseconds) for completing all the close-related operations. Use -1 for infinity.
    # + return - An error if an I/O problem is encountered.
    public function close(int? closeCode = (), string? closeMessage = (), int? timeout = ())
                    returns Error? = external;

    # Aborts the RabbitMQ `Connection` and all its `Channel`s.
    # Forces the `Connection` to close and waits for all the close operations to complete. When timeout is reached
    # the socket is forced to close. Any encountered exceptions in the close operations are silently discarded.
    #
    # + closeCode - The close code (For information, go to the "Reply Codes" section in the
    #               [AMQP 0-9-1 specification] (#https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)).
    # + closeMessage - A message indicating the reason for closing the connection.
    # + timeout - Timeout (in milliseconds) for completing all the close-related operations. Use -1 for infinity.
    # + return - An error if an I/O problem is encountered.
    public function abortConnection(int? closeCode = (), string? closeMessage = (), int? timeout = ())
                                                                                    returns Error? = external;

    # Checks whether `close` was already called.
    #
    # + return - The value `true` if the `Connection` is already closed and `false` otherwise.
    public function isClosed() returns boolean = external;

    private function createConnection(ConnectionConfiguration connectionConfiguration) = external;
};
