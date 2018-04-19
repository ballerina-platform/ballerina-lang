package ballerina.jms;

import ballerina/log;

public type SimpleQueueSender object {
    public {
        SimpleQueueSenderEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        QueueSender? sender;
    }

    public function init(SimpleQueueSenderEndpointConfiguration config) {
        self.config = config;
        Connection conn = new ({
                initialContextFactory: config.initialContextFactory,
                providerUrl: config.providerUrl,
                connectionFactoryName: config.connectionFactoryName,
                properties: config.properties
            });
        self.connection = conn;

        Session newSession = new (conn, {
                acknowledgementMode: config.acknowledgementMode
            });
        self.session = newSession;

        QueueSender queueSender = new;
        QueueSenderEndpointConfiguration senderConfig = {
            session: newSession,
            queueName: config.queueName
        };
        queueSender.init(senderConfig);
        self.sender = queueSender;
    }

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    public function getCallerActions() returns QueueSenderConnector {
        match (sender) {
            QueueSender s => return s.getCallerActions();
            () => {
                error e = {message: "Queue sender cannot be nil"};
                throw e;
            }
        }
    }

    public function stop() {
    }

    public function createTextMessage(string message) returns Message|error {
        match (session) {
            Session s => return s.createTextMessage(message);
            () => {
                error e = {message: "Session cannot be nil"};
                throw e;
            }
        }

    }
};

public type SimpleQueueSenderEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string queueName;
};

