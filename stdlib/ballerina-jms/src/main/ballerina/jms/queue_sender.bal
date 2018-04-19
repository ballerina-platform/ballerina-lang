package ballerina.jms;

import ballerina/log;

public type QueueSender object {
    public {
        QueueSenderConnector connector;
        QueueSenderEndpointConfiguration config;
    }

    new () {
        self.connector = new;
    }

    public function init(QueueSenderEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => self.initQueueSender(s);
            () => {}
        }
    }

    public native function initQueueSender(Session session);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    public function getCallerActions() returns QueueSenderConnector {
        return self.connector;
    }

    public function stop() {
    }
};

public type QueueSenderEndpointConfiguration {
    Session? session;
    string queueName;
};

public type QueueSenderConnector object {
    public native function send(Message m) returns error?;
};
