package ballerina.mb;

import ballerina/jms;
import ballerina/log;

public type SimpleTopicSubscriber object {

    public {
        SimpleTopicSubscriberEndpointConfiguration config;
    }

    private {
        jms:SimpleTopicSubscriber subscriber;
        TopicSubscriberConnector? connector;
    }

    public function init(SimpleTopicSubscriberEndpointConfiguration config) {
        self.config = config;
        self.subscriber.init({
                initialContextFactory:"wso2mbInitialContextFactory",
                providerUrl:generateBrokerURL(config),
                connectionFactoryName:config.connectionFactoryName,
                acknowledgementMode:config.acknowledgementMode,
                messageSelector:config.messageSelector,
                properties:config.properties,
                topicPattern:config.topicPattern
            }
        );
        self.connector = new TopicSubscriberConnector(self.subscriber.getClient());
    }

    public function register (typedesc serviceType) {
        self.subscriber.register(serviceType);
    }

    public function start () {
        self.subscriber.start();
    }

    public function getClient () returns (TopicSubscriberConnector) {
        match (self.connector) {
            TopicSubscriberConnector c => return c;
            () => {
                error e = {message:"Topic subscriber connector cannot be nil."};
                throw e;
            }
        }
    }

    public function stop () {
        self.subscriber.stop();
    }

    public function createTextMessage(string message) returns (Message | Error) {
        var result = self.subscriber.createTextMessage(message);
        match (result) {
            jms:Message m => return new Message(m);
            Error e => return e;
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
            jms:Message m => return new Message(m);
            jms:Error e => return e;
            () => return ();
        }
    }
};


