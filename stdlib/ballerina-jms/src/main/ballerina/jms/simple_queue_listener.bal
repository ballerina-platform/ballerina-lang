package ballerina.jms;

import ballerina/log;

public type SimpleQueueListener object {
    public {
        SimpleQueueListenerEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        QueueConsumer? consumer;
    }

    public function init(SimpleQueueListenerEndpointConfiguration config) {
        self.config = config;
        Connection conn = new ({
                initialContextFactory: config.initialContextFactory,
                providerUrl: config.providerUrl,
                connectionFactoryName: config.connectionFactoryName,
                properties: config.properties
            });
        connection = conn;

        Session newSession = new (conn, {
                acknowledgementMode: config.acknowledgementMode
            });
        session = newSession;

        QueueConsumer queueConsumer = new;
        QueueConsumerEndpointConfiguration consumerConfig = {
            session: newSession,
            queueName: config.queueName,
            messageSelector: config.messageSelector
        };
        queueConsumer.init(consumerConfig);
        consumer = queueConsumer;
    }

    public function register (typedesc serviceType) {
        match (consumer) {
            QueueConsumer c => {
                c.register(serviceType);
            }
            () => {
                error e = {message: "Queue consumer cannot be nil"};
                throw e;
            }
        }
    }

    public function start () {
    }

    public function getClient () returns (QueueConsumerConnector) {
        match (consumer) {
            QueueConsumer c => return c.getClient();
            () => {
                error e = {message: "Queue consumer cannot be nil"};
                throw e;
            }
        }
    }

    public function stop () {
    }

    public function createTextMessage(string message) returns (Message) {
        match (session) {
            Session s => return s.createTextMessage(message);
            () => {
                error e = {message: "Session cannot be null"};
                throw e;
            }
        }

    }
};

public type SimpleQueueListenerEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string messageSelector;
    map properties;
    string queueName;
};

