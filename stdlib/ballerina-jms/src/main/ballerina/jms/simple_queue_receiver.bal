package ballerina.jms;

import ballerina/log;

public type SimpleQueueReceiver object {
    public {
        SimpleQueueReceiverEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        QueueReceiver? queueReceiver;
    }

    public function init(SimpleQueueReceiverEndpointConfiguration config) {
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

        QueueReceiver receiver = new;
        QueueReceiverEndpointConfiguration queueReceiverConfig = {
            session: newSession,
            queueName: config.queueName,
            messageSelector: config.messageSelector
        };
        receiver.init(queueReceiverConfig);
        self.queueReceiver = receiver;
    }

    public function register(typedesc serviceType) {
        match (queueReceiver) {
            QueueReceiver c => {
                c.register(serviceType);
            }
            () => {
                error e = {message: "Queue receiver cannot be nil"};
                throw e;
            }
        }
    }

    public function start() {
    }

    public function getCallerActions() returns QueueReceiverActions {
        match (queueReceiver) {
            QueueReceiver c => return c.getCallerActions();
            () => {
                error e = {message: "Queue receiver cannot be nil"};
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
                error e = {message: "Session cannot be null"};
                throw e;
            }
        }

    }
};

public type SimpleQueueReceiverEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector;
    map properties;
    string queueName;
};
