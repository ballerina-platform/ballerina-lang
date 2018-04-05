package ballerina.jms;

import ballerina/log;

public type QueueSender object {
    public {
        QueueSenderConnector connector;
        QueueSenderEndpointConfiguration config;
    }

    new () {
        self.connector = new ();
    }

    public function init(QueueSenderEndpointConfiguration config) {
        self.config = config;
        SessionConnector sessionConnector = config.session.getClient();
        self.initQueueSender(sessionConnector);
    }

    public native function initQueueSender(SessionConnector connector);

    public function register (typedesc serviceType) {
    }

    public function start () {
    }

    public function getClient () returns (QueueSenderConnector) {
        return self.connector;
    }

    public function stop () {
    }
}

public type QueueSenderEndpointConfiguration {
    Session session;
    string queueName;
}

public type QueueSenderConnector object {
    public native function send (Message m);
}


