package ballerina.jms;

import ballerina/log;

public type QueueReceiver object {

    public {
        QueueReceiverConnector connector;
        QueueReceiverEndpointConfiguration config;
    }

    public function init(QueueReceiverEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => {
                self.createQueueReceiver(s, config.messageSelector);
                log:printInfo("Message receiver created for queue " + config.queueName);
            }
            () => { log:printInfo("Message receiver not properly initialised for queue " + config.queueName); }
        }

    }

    public function register (typedesc serviceType) {
        self.registerListener(serviceType, connector);
    }

    native function registerListener(typedesc serviceType, QueueReceiverConnector connector);

    native function createQueueReceiver (Session session, string messageSelector);

    public function start () {
    }

    public function getCallerActions () returns (QueueReceiverConnector) {
        return connector;
    }

    public function stop () {
        self.closeQueueReceiver(connector);
    }

    native function closeQueueReceiver(QueueReceiverConnector connector);
};

public type QueueReceiverEndpointConfiguration {
    Session? session;
    string queueName;
    string messageSelector;
    string identifier;
};

public type QueueReceiverConnector object {
    public native function acknowledge (Message message) returns (Error | ());

    public native function receive (int timeoutInMilliSeconds = 0) returns (Message | Error | () );
};
