package ballerina.jms;

import ballerina/log;

public type SimpleDurableTopicListener object {
    public {
        SimpleDurableTopicListenerEndpointConfiguration config;
    }

    private {
        Connection? connection;
        Session? session;
        DurableTopicSubscriber? subscriber;
    }

    public function init(SimpleDurableTopicListenerEndpointConfiguration config) {
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

        DurableTopicSubscriber topicSubscriber = new;
        DurableTopicSubscriberEndpointConfiguration consumerConfig = {
            session: newSession,
            topicPattern: config.topicPattern,
            messageSelector: config.messageSelector,
            identifier: config.identifier
        };
        topicSubscriber.init(consumerConfig);
        subscriber = topicSubscriber;
    }

    public function register (typedesc serviceType) {
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

    public function start () {
    }

    public function getClient () returns (DurableTopicSubscriberConnector) {
        match (subscriber) {
            DurableTopicSubscriber c => return c.getClient();
            () => {
                error e = {message: "Topic subscriber cannot be nil"};
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

public type SimpleDurableTopicListenerEndpointConfiguration {
    string initialContextFactory = "wso2mbInitialContextFactory";
    string providerUrl = "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'";
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier,
    map properties;
    string messageSelector;
    string topicPattern;
};

