package ballerina.jms;

import ballerina/log;

public type SimpleTopicProducer object {
    public {
        SimpleTopicProducerEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        TopicProducer? producer;
    }

    public function init(SimpleTopicProducerEndpointConfiguration config) {
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

        TopicProducer topicProducer = new;
        TopicProducerEndpointConfiguration producerConfig = {
            session: newSession,
            topicPattern: config.topicPattern
        };
        topicProducer.init(producerConfig);
        producer = topicProducer;
    }

    public function register (typedesc serviceType) {
    }

    public function start () {
    }

    public function getClient () returns (TopicProducerConnector) {
        match (producer) {
            TopicProducer s => return s.getClient();
            () => {
                error e = {message: "Topic producer cannot be nil"};
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
                error e = {message: "Session cannot be nil"};
                throw e;
            }
        }

    }
};

public type SimpleTopicProducerEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string topicPattern;
};

