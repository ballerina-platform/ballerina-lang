package ballerina.jms;

import ballerina/log;

public type SimpleDurableTopicSubscriber object {
    public {
        SimpleDurableTopicSubscriberEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        DurableTopicSubscriber? subscriber;
    }

    public function init(SimpleDurableTopicSubscriberEndpointConfiguration config) {
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

        DurableTopicSubscriber topicSubscriber = new;
        DurableTopicSubscriberEndpointConfiguration consumerConfig = {
            session: newSession,
            topicPattern: config.topicPattern,
            messageSelector: config.messageSelector,
            identifier: config.identifier
        };
        topicSubscriber.init(consumerConfig);
        self.subscriber = topicSubscriber;
    }

    public function register(typedesc serviceType) {
        match (subscriber) {
            DurableTopicSubscriber c => {
                c.register(serviceType);
            }
            () => {
                error e = {message: "Topic Subscriber cannot be nil"};
                throw e;
            }
        }
    }

    public function start() {
    }

    public function getCallerActions() returns DurableTopicSubscriberActions {
        match (subscriber) {
            DurableTopicSubscriber c => return c.getCallerActions();
            () => {
                error e = {message: "Topic subscriber cannot be nil"};
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

public type SimpleDurableTopicSubscriberEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier,
    map properties;
    string messageSelector;
    string topicPattern;
};
