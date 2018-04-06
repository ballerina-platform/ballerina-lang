package ballerina.jms;

import ballerina/log;

public type QueueConsumer object {

    public {
        QueueConsumerConnector connector;
        QueueConsumerEndpointConfiguration config;
    }

    public function init(QueueConsumerEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => {
                createConsumer(s);
                log:printInfo("Consumer created for queue " + config.queueName);
            }
            () => {}
        }

    }

    public function register (typedesc serviceType) {
        registerListener(serviceType, connector);
    }

    native function registerListener(typedesc serviceType, QueueConsumerConnector connector);

    native function createConsumer (Session session);

    public function start () {
    }

    public function getClient () returns (QueueConsumerConnector) {
        return connector;
    }

    public function stop () {
    }
};

public type QueueConsumerEndpointConfiguration {
    Session? session;
    string queueName;
    string identifier;
};

public type QueueConsumerConnector object {
};
