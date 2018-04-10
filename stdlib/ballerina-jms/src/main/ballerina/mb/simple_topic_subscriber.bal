package ballerina.mb;

import ballerina/jms;
import ballerina/log;

public type SimpleTopicSubscriber object {

    public {
        SimpleTopicSubscriberEndpointConfiguration config;
    }

    private {
        jms:Connection? connection;
        jms:Session? session;
        jms:TopicSubscriber? subscriber;
    }

    public function init(SimpleTopicSubscriberEndpointConfiguration config) {
        self.config = config;
        jms:Connection conn = new ({
                initialContextFactory: "wso2mbInitialContextFactory",
                providerUrl: generateBrokerURL(config),
                connectionFactoryName: config.connectionFactoryName,
                properties: config.properties
            });
        self.connection = conn;

        jms:Session newSession = new (conn, {
                acknowledgementMode: config.acknowledgementMode
            });
        self.session = newSession;

        jms:TopicSubscriber topicSubscriber = new;
        jms:TopicSubscriberEndpointConfiguration consumerConfig = {
            session: newSession,
            topicPattern: config.topicPattern,
            messageSelector: config.messageSelector
        };
        topicSubscriber.init(consumerConfig);
        self.subscriber = topicSubscriber;
    }

    public function register (typedesc serviceType) {
        match (subscriber) {
            jms:TopicSubscriber c => {
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

    public function getClient () returns (TopicSubscriberConnector) {
        match (subscriber) {
            jms:TopicSubscriber c => return new TopicSubscriberConnector(c.getClient());
            () => {
                error e = {message: "Topic subscriber cannot be nil"};
                throw e;
            }
        }
    }

    public function stop () {
    }

    public function createTextMessage(string message) returns (Message | Error) {
        match (session) {
            jms:Session s => {
                var result = s.createTextMessage(message);
                match (result) {
                    jms:Message m => return new Message(m);
                    Error err => return err;
                }
            }
            () => {
                error e = {message:"Session cannot be null"};
                throw e;
            }
        }
    }
};

public type SimpleTopicSubscriberEndpointConfiguration {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    string connectionFactoryName = "ConnectionFactory",
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    string messageSelector,
    map properties,
    string topicPattern,
};

public type TopicSubscriberConnector object {

    public {
        jms:TopicSubscriberConnector helper;
    }

    public new(helper) {}

    public function acknowledge (Message message) returns Error|() {
        return self.helper.acknowledge(message.getJMSMessage());
    }

    public function receive (int timeoutInMilliSeconds = 0) returns Message|Error|() {
        var result = self.helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => {
                return new Message(m);
            }
            jms:Error e => return e;
            () => {}
        }
    }
};


