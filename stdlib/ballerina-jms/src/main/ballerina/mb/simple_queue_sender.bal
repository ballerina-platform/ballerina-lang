
import ballerina/log;

public type SimpleQueueSender object {
    public {
        SimpleQueueSenderEndpointConfiguration config;
    }

    private {
        jms:SimpleQueueSender? sender;
        QueueSenderActions? producerActions;
    }

    public function init(SimpleQueueSenderEndpointConfiguration config) {
        endpoint jms:SimpleQueueSender queueSender {
            initialContextFactory: "wso2mbInitialContextFactory",
            providerUrl: generateBrokerURL(config),
            connectionFactoryName: "ConnectionFactory",
            acknowledgementMode: config.acknowledgementMode,
            properties: config.properties,
            queueName: config.queueName
        };
        self.sender = queueSender;
        self.producerActions = new QueueSenderActions(queueSender);
        self.config = config;
    }

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    public function getCallerActions() returns QueueSenderActions {
        match (self.producerActions) {
            QueueSenderActions s => return s;
            () => {
                error e = {message: "Queue sender connector cannot be nil"};
                throw e;
            }
        }
    }

    public function stop() {
    }

    public function createTextMessage(string message) returns Message|error {
        match (self.sender) {
            jms:SimpleQueueSender s => {
                var result = s.createTextMessage(message);
                match(result) {
                    jms:Message m => return new Message(m);
                    error e => return e;
                }
            }
            () => {
                error e = {message: "Session cannot be nil"};
                throw e;
            }
        }

    }
};

public type QueueSenderActions object {
    private {
        jms:SimpleQueueSender sender;
    }

    new (sender) {}

    public function send(Message m) returns error? {
        endpoint jms:SimpleQueueSender senderEP = self.sender;
        var result = senderEP->send(m.getJMSMessage());
        return result;
    }
};

public type SimpleQueueSenderEndpointConfiguration {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    map properties,
    string queueName,
};
