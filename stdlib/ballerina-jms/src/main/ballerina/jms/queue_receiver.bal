package ballerina.jms;

import ballerina/log;

public type QueueReceiver object {

    public {
        QueueReceiverActions consumerActions;
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

    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    native function registerListener(typedesc serviceType, QueueReceiverActions consumerActions);

    native function createQueueReceiver(Session session, string messageSelector);

    public function start() {
    }

    public function getCallerActions() returns QueueReceiverActions {
        return consumerActions;
    }

    public function stop() {
        self.closeQueueReceiver(consumerActions);
    }

    native function closeQueueReceiver(QueueReceiverActions consumerActions);
};

public type QueueReceiverEndpointConfiguration {
    Session? session;
    string queueName;
    string messageSelector;
    string identifier;
};

public type QueueReceiverActions object {
    public native function acknowledge(Message message) returns error?;

    public native function receive(int timeoutInMilliSeconds = 0) returns Message|error|();
};
