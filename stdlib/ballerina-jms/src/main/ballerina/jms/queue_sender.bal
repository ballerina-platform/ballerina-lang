
import ballerina/log;

public type QueueSender object {
    public {
        QueueSenderActions producerActions;
        QueueSenderEndpointConfiguration config;
    }

    new () {
        self.producerActions = new;
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

    public function getCallerActions() returns QueueSenderActions {
        return self.producerActions;
    }

    public function stop() {
    }
};

public type QueueSenderEndpointConfiguration {
    Session? session;
    string queueName;
};

public type QueueSenderActions object {
    public native function send(Message m) returns error?;
};
