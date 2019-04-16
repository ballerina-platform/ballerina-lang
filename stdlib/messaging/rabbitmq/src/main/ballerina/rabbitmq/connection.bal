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
    # + timeout - Timeout (in milliseconds) for completing all the close-related operations, use -1 for infinity.
    # + return - An `error` if an I/O problem is encountered.
    public function close(int? timeout = ()) returns error? = external;

    # Checks whether `close` was already called.
    #
    # + return - The value `true` if the `Connection` is already closed and `false` otherwise.
    public function isClosed() returns boolean = external;

    # Extern function called to initialize the RabbitMQ `Connection`.
    #
    # + connectionConfiguration - Holds the connection parameters required to initialize the `Connection`.
    private function createConnection(ConnectionConfiguration connectionConfiguration) = external;
};

# Holds the parameters used to create a RabbitMQ `Connection`.
#
# + host - The host used for establishing the connection.
# + port - The port used for establishing the connection.
# + username - The username used for establishing the connection.
# + password - The password used for establishing the connection.
# + connectionTimeout - Connection TCP establishment timeout in milliseconds; zero for infinite.
# + handshakeTimeout -  The AMQP 0-9-1 protocol handshake timeout, in milliseconds.
# + shutdownTimeout - Shutdown timeout in milliseconds; zero for infinite; default 10000. If consumers exceed
#                     this timeout then any remaining queued deliveries (and other Consumer callbacks) will be lost.
# + heartbeat - The initially requested heartbeat timeout, in seconds; zero for none.
public type ConnectionConfiguration record {|
    string host;
    int port = 5672;
    string? username = ();
    string? password = ();
    int? connectionTimeout = ();
    int? handshakeTimeout = ();
    int? shutdownTimeout = ();
    int? heartbeat = ();
|};
