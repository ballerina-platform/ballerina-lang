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
    private boolean anonymousSession;

    public function __init(Session | URLConfiguration sesssionOrURLConfig) {
        if (sesssionOrURLConfig is Session) {
            self.session = sesssionOrURLConfig;
        } else {
            Connection connection = new("tcp://" + sesssionOrURLConfig.host + ":" + sesssionOrURLConfig.port);
            self.session = new(connection, config = { username: sesssionOrURLConfig["username"],
                    password: sesssionOrURLConfig["password"] });
            self.anonymousSession = true;
        }
    }
    public function __start() returns error? {
        return self.start();
    }
    public function __stop() returns error? {
        return self.stop();
    }
    public function __attach(service serviceType, map<any> annotationData) returns error? {
        return self.createConsumer(serviceType);
    }

    extern function start() returns error?;
    extern function createConsumer(service serviceType) returns error?;
    extern function stop() returns error?;
};

# The configuration for an Artemis consumer service.
#
# + autoAck - whether to automatically acknowledge a service when a resource completes
# + queueConfig - the configuration for the queue to consume from
# + filter - only messages which match this filter will be consumed
# + browseOnly - whether the ClientConsumer will only browse the queue or consume messages
public type ArtemisServiceConfig record {
    boolean autoAck = true;
    QueueConfiguration queueConfig;
    string? filter = ();
    boolean browseOnly = false;
    !...;
};

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
public type QueueConfiguration record {
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
    !...;
};
