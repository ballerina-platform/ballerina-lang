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

# Represents ActiveMQ Artemis Listener. This is an abstraction for that includes the connection and session. 
# Consumers are represented by the service attaching to this listener.
public type Listener object {
    *AbstractListener;
    private Session session;

    public function __init(Session | EndpointConfiguration sessionOrEndpointConfig) {
        if (sessionOrEndpointConfig is Session) {
            self.session = sessionOrEndpointConfig;
        } else {
            Connection connection;
            var secureSocket = sessionOrEndpointConfig["secureSocket"];
            if (secureSocket is SecureSocket) {
                connection = new(parseUrl(sessionOrEndpointConfig), config = {secureSocket: secureSocket});
            } else {
                connection = new(parseUrl(sessionOrEndpointConfig));
            }
            self.session = new(connection, config = { username: sessionOrEndpointConfig["username"],
                    password: sessionOrEndpointConfig["password"],
                    autoCommitSends: false, autoCommitAcks:  false });
            self.session.anonymousSession = true;
        }
    }
    public function __start() returns error? {
        return self.start();
    }
    public function __stop() returns error? {
        return self.stop();
    }
    public function __attach(service serviceType, string? name = ()) returns error? {
        return self.createConsumer(serviceType);
    }

    # The Artemis consumer is represented by the `Service` object.
    # However, if there is a need to make blocking synchronous calls, within a transaction block,
    # this method can be used to create and get a `Consumer` object.
    # 
    # + config - the configuration for the queue
    # + autoAck - whether to acknowledge the received message automatically
    # + filter - only messages, which match this filter will be consumed
    # + return - the `Consumer` object for the listener to make blocking calls
    public function createAndGetConsumer(QueueConfiguration config, boolean autoAck = true, string? filter = ())
    returns Consumer | error {
        return new Consumer(self.session, config, autoAck, filter);
    }

    private function start() returns error? = external;
    private function createConsumer(service serviceType) returns error? = external;
    private function stop() returns error? = external;
};

# The configuration for an Artemis consumer service.
#
# + autoAck - whether to acknowledge a message automatically when a resource completes
# + queueConfig - the configuration for the queue to consume from
# + filter - only messages which match this filter will be consumed
public type ArtemisServiceConfig record {|
    boolean autoAck = true;
    QueueConfiguration queueConfig;
    string? filter = ();
|};

public annotation<service> ServiceConfig ArtemisServiceConfig;

# ActiveMQ Artemis Queue configuration.
# If the `autoCreated` is `false` an error will be thrown if the queue does not exist.
# If `autocreated` is `true` and the queue already exists then the other configurations would be ignored.
#
# + queueName - the name of the queue
# + addressName - the address queue is bound to. If the value is `nil` and `autoCreated` is true and the 
# queue does not already exist then the address would take the name of the queue.
# + autoCreated - whether to automatically create the queue
# + routingType - the routing type for the queue, MULTICAST or ANYCAST
# + temporary - whether the queue is temporary. If this value is set to true the `durable` property value shall be ignored.
# + filter - messages which match this filter will be put in the queue
# + durable - whether the queue is durable or not. If `temporary` property value is true this value 
# + maxConsumers - how many concurrent consumers will be allowed on this queue
# + purgeOnNoConsumers - whether to delete the contents of the queue when the last consumer disconnects
# + exclusive - whether the queue should be exclusive
# + lastValue - whether the queue should be lastValue
public type QueueConfiguration record {|
    string queueName;
    string? addressName = ();
    boolean autoCreated = true;
    RoutingType routingType = ANYCAST;
    boolean temporary = true;
    string? filter = ();
    boolean durable = false;
    int maxConsumers = -1;
    boolean purgeOnNoConsumers = false;
    boolean exclusive = false;
    boolean lastValue = false;
|};

# Representation of the ActiveMQ Artemis client consumer to make blocking receive calls.
public type Consumer client object {
    function __init(Session sessObj, QueueConfiguration config, boolean autoAck, string? filter)
    returns error? {
        check self.createConsumer(sessObj, config, autoAck, filter);
    }

    private function createConsumer(Session sessionObj, QueueConfiguration queueConfig, boolean autoAck,
     string? consumerFilter) returns error? = external;
    public remote function receive(int timeoutInMilliSeconds = 0) returns @tainted Message | error = external;
};
