package ballerina.jms;

import ballerina/jms;
import ballerina/log;

public type SimpleDurableTopicSubscriber object {
    public {
        SimpleDurableTopicSubscriberEndpointConfiguration config;
    }

    private {
        jms:SimpleDurableTopicListener subscriber;
        DurableTopicSubscriberConnector? connector;
    }

    public function init(SimpleDurableTopicSubscriberEndpointConfiguration config) {
        self.config = config;
        self.subscriber.init({
                initialContextFactory:"wso2mbInitialContextFactory",
                providerUrl:generateBrokerURL(config),
                acknowledgementMode:config.acknowledgementMode,
                identifier:config.identifier,
                properties:config.properties,
                messageSelector:config.messageSelector,
                topicPattern:config.topicPattern
            });
        self.connector = new DurableTopicSubscriberConnector(self.subscriber.getClient());
    }

    public function register (typedesc serviceType) {
        self.subscriber.register(serviceType);
    }

    public function start () {
        self.subscriber.start();
    }

    public function getClient () returns (DurableTopicSubscriberConnector) {
        match (self.connector) {
            DurableTopicSubscriberConnector c => return c;
            () => {
                error e = {message:"Durable topic subscriber connector cannot be nil."};
                throw e;
            }
        }
    }

    public function stop () {
        self.subscriber.stop();
    }

    public function createTextMessage(string message) returns (Message|Error) {
        var result = self.subscriber.createTextMessage(message);
        match (result) {
            jms:Message m => return new Message(m);
            Error e => return e;
        }
    }
};

public type SimpleDurableTopicSubscriberEndpointConfiguration {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    string connectionFactoryName = "ConnectionFactory";
    string acknowledgementMode = "AUTO_ACKNOWLEDGE";
    string identifier,
    map properties;
    string messageSelector;
    string topicPattern;
};

public type DurableTopicSubscriberConnector object {

    public {
        jms:DurableTopicSubscriberConnector helper;
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
