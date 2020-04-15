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

# Represents a single network connection to the RabbitMQ broker.
public type Connection object {

    handle amqpConnection;

    # Initializes a Ballerina RabbitMQ Connection object.
    #
    # + connectionConfiguration - Configurations required to initialize the `rabbitmq:Connection`
    public function __init(ConnectionConfiguration connectionConfiguration) {
        self.amqpConnection = createConnection(connectionConfiguration);
    }

# Closes the `rabbitmq:Connection` and all its `rabbitmq:Channel`s.
# It waits with a provided timeout for all the close operations to complete.
# When the timeout is reached, the socket is forced to close.
# ```ballerina
# rabbitmq:Error? closeResult = newConnection.close();
# ```
#
# + closeCode - The close code (for information, go to the "Reply Codes" section in the
#               [AMQP 0-9-1 specification] (#https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf))
# + closeMessage - A message indicating the reason for closing the connection
# + timeoutInMillis - Timeout (in milliseconds) for completing all the close-related operations.
#                     Use -1 for infinity
# + return - A `rabbitmq:Error` if an I/O error is encountered or else `()`
    public function close(int? closeCode = (), string? closeMessage = (), int? timeoutInMillis = ()) returns Error? {
        return handleCloseConnection(closeCode, closeMessage, timeoutInMillis, self.amqpConnection);
    }

# Aborts the `rabbitmq:Connection` and all its `rabbitmq:Channel`s.
# Forces the `rabbitmq:Connection` to close and waits for all the close operations to complete. When the timeout is reached,
# the socket is forced to close. Any encountered exceptions in the close operations are discarded silently.
# ```ballerina
# connection.abortConnection(320, "Channel Aborted");
# ```
#
# + closeCode - The close code (for information, go to the "Reply Codes" section in the
#               [AMQP 0-9-1 specification] (#https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf))
# + closeMessage - A message indicating the reason for closing the connection
# + timeoutInMillis - Timeout (in milliseconds) for completing all the close-related operations.
#                       Use -1 for infinity
    public function abortConnection(int? closeCode = (), string? closeMessage = (), int? timeoutInMillis = ()) {
        handleAbortConnection(closeCode, closeMessage, timeoutInMillis, self.amqpConnection);
    }

# Checks whether `close` was already called.
# ```ballerina
# boolean closed = newConnection.isClosed();
# ```
#
# + return - The value `true` if the `rabbitmq:Connection` is already closed or else `false`
    public function isClosed() returns boolean {
        return nativeIsClosed(self.amqpConnection);
    }
};

function createConnection(ConnectionConfiguration connectionConfiguration) returns handle =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ConnectionUtils"
} external;

function nativeIsClosed(handle amqpConnection) returns boolean =
@java:Method {
    name: "isClosed",
    class: "org.ballerinalang.messaging.rabbitmq.util.ConnectionUtils"
} external;

function handleCloseConnection(int? closeCode, string? closeMessage, int? timeout, handle amqpConnection)
returns Error? =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ConnectionUtils"
} external;

function handleAbortConnection(int? closeCode, string? closeMessage, int? timeout, handle amqpConnection) =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ConnectionUtils"
} external;
