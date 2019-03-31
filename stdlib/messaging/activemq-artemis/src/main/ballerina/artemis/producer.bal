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
    private boolean anonymousSession = false;

    public function __init(Session | URLConfiguration sesssionOrURLConfig, string addressName,
                           AddressConfiguration? addressConfig = (), int rate = -1) {
        if (sesssionOrURLConfig is Session) {
            self.session = sesssionOrURLConfig;
        } else {
            Connection connection = new("tcp://" + sesssionOrURLConfig.host + ":" + sesssionOrURLConfig.port);
            self.session = new(connection, config = { username: sesssionOrURLConfig["username"],
                    password: sesssionOrURLConfig["password"] });
            self.anonymousSession = true;
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
    public remote function send(int | float | string | json | xml | byte | byte[] | map<string | int | float | byte
| boolean | byte[]> | io:ReadableByteChannel | Message data) returns error? {
        return self.externSend(data is Message ? data : new(self.session, data));
    }

    # Returns whether the producer is closed or not
    # 
    # + return - `true` if the producer is closed and `false` otherwise
    public function isClosed() returns boolean = external;

    # Closes the ClientProducer. If already closed nothing is done.
    # 
    # + return - `error` on failure to close.
    public remote function close() returns error? = external;

    function externSend(Message data) returns error? = external;
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
