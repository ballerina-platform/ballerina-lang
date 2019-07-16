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

# Represents ActiveMQ Artemis Producer.
public type Producer client object {
    private Session session;

    public function __init(Session | EndpointConfiguration sessionOrEndpointConfig, string addressName,
                           AddressConfiguration? addressConfig = (), int rate = -1) {
        if (sessionOrEndpointConfig is Session) {
            self.session = sessionOrEndpointConfig;
        } else {
            Connection connection;
            var secureSocket = sessionOrEndpointConfig["secureSocket"];
            if (secureSocket is SecureSocket) {
                connection = new(parseUrl(sessionOrEndpointConfig), {secureSocket: secureSocket});
            } else {
                connection = new(parseUrl(sessionOrEndpointConfig));
            }
            self.session = new(connection, { username: sessionOrEndpointConfig["username"],
                    password: sessionOrEndpointConfig["password"],
                    autoCommitSends: false, autoCommitAcks:  false});
            self.session.anonymousSession = true;
        }
        AddressConfiguration configuration = {

        };
        if (addressConfig is AddressConfiguration) {
            configuration = addressConfig;
        }
        self.createProducer(addressName, configuration, rate);
    }

    function createProducer(string addressName, AddressConfiguration addressConfig, int rate) = external;

    # Sends a message to the producer's address.
    #
    # + data - the `Message` or data to send 
    # + return - `error` on failure
    public remote function send(MessageContent | Message data) returns ArtemisError? {
        return self.externSend(data is Message ? data : new(self.session, data));
    }

    # Returns whether the producer is closed or not
    # 
    # + return - `true` if the producer is closed and `false` otherwise
    public function isClosed() returns boolean = external;

    # Closes the ClientProducer. If already closed nothing is done.
    # 
    # + return - `error` on failure to close.
    public remote function close() returns ArtemisError? = external;

    function externSend(Message data) returns ArtemisError? = external;
};

# The ActiveMQ Artemis address related configuration.
# If the `autoCreated` is `false` an error will be thrown if the address does not exist.
# If `autocreated` is `true` and the address already exists then the `routingType` configuration would be ignored.
#
# + routingType - the routing type for the address, MULTICAST or ANYCAST
# + autoCreated - whether the address has to be auto created. 
public type AddressConfiguration record {|
    RoutingType routingType = ANYCAST;
    boolean autoCreated = true;
|};
