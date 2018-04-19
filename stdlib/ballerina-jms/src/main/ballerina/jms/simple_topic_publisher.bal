package ballerina.jms;

import ballerina/log;

public type SimpleTopicPublisher object {
    public {
        SimpleTopicPublisherEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        TopicPublisher? publisher;
    }

    public function init(SimpleTopicPublisherEndpointConfiguration config) {
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

        TopicPublisher topicPublisher = new;
        TopicPublisherEndpointConfiguration publisherConfig = {
            session: newSession,
            topicPattern: config.topicPattern
        };
        topicPublisher.init(publisherConfig);
        self.publisher = topicPublisher;
    }

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    public function getCallerActions() returns TopicPublisherConnector {
        match (publisher) {
            TopicPublisher s => return s.getCallerActions();
            () => {
                error e = {message: "Topic publisher cannot be nil"};
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

public type SimpleTopicPublisherEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    map properties;
    string topicPattern;
};
