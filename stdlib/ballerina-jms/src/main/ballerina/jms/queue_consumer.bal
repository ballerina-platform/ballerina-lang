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
                self.createConsumer(s, config.messageSelector);
                log:printInfo("Consumer created for queue " + config.queueName);
            }
            () => {}
        }

    }

    public function register (typedesc serviceType) {
        self.registerListener(serviceType, connector);
    }

    native function registerListener(typedesc serviceType, QueueConsumerConnector connector);

    native function createConsumer (Session session, string messageSelector);

    public function start () {
    }

    public function getClient () returns (QueueConsumerConnector) {
        return connector;
    }

    public function stop () {
        self.closeConsumer(connector);
    }

    native function closeConsumer(QueueConsumerConnector connector);
};

public type QueueConsumerEndpointConfiguration {
    Session? session;
    string queueName;
    string messageSelector;
    string identifier;
};

public type QueueConsumerConnector object {
    public native function acknowledge (Message message) returns (Error | ());

    public native function receive (int timeoutInMilliSeconds = 0) returns (Message | Error | () );
};
