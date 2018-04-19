package ballerina.jms;

import ballerina/log;

public type SimpleTopicPublisher object {
    public {
        SimpleTopicPublisherEndpointConfiguration config;
    }

    private {
        jms:SimpleTopicPublisher? publisher;
        TopicPublisherConnector? publisherConnector;
    }

    public function init(SimpleTopicPublisherEndpointConfiguration config) {
    endpoint jms:SimpleTopicPublisher topicPublisher {
        initialContextFactory: "wso2mbInitialContextFactory",
        providerUrl: generateBrokerURL(config),
        connectionFactoryName: "ConnectionFactory",
        acknowledgementMode: config.acknowledgementMode,
        properties: config.properties,
        topicPattern: config.topicPattern
    };
    self.publisher = topicPublisher;
    self.publisherConnector = new TopicPublisherConnector(topicPublisher);
    self.config = config;
}

    public function register (typedesc serviceType) {
    }

    public function start () {
    }

    public function getCallerActions () returns (TopicPublisherConnector) {
        match (self.publisherConnector) {
            TopicPublisherConnector s => return s;
            () => {
                error e = {message:"Topic publisher connector cannot be nil"};
                throw e;
            }
        }
    }

    public function stop () {
    }

    public function createTextMessage(string message) returns (Message|Error) {
        match (self.publisher) {
            jms:SimpleTopicPublisher s => {
                var result = s.createTextMessage(message);
                match (result) {
                    jms:Message m => return new Message(m);
                    jms:Error e => return e;
                }
            }
            () => {
                error e = {message:"topic publisher cannot be nil"};
                throw e;
            }
        }

    }
};

public type TopicPublisherConnector object {
    private {
        jms:SimpleTopicPublisher publisher;
    }

    new (publisher) {}

    public function send (Message m) returns (Error | ()) {
        endpoint jms:SimpleTopicPublisher publisherEP = self.publisher;
        var result = publisherEP->send(m.getJMSMessage());
        return result;
    }
};

public type SimpleTopicPublisherEndpointConfiguration {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
    string acknowledgementMode = "AUTO_ACKNOWLEDGE",
    map properties,
    string topicPattern,
};

