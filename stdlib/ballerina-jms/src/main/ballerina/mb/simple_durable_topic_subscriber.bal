
import ballerina/jms;
import ballerina/log;

public type SimpleDurableTopicSubscriber object {
    public {
        SimpleDurableTopicSubscriberEndpointConfiguration config;
    }

    private {
        jms:SimpleDurableTopicSubscriber subscriber;
        DurableTopicSubscriberActions? consumerActions;
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
        self.consumerActions = new DurableTopicSubscriberActions(self.subscriber.getCallerActions());
    }

    public function register(typedesc serviceType) {
        self.subscriber.register(serviceType);
    }

    public function start() {
        self.subscriber.start();
    }

    public function getCallerActions() returns DurableTopicSubscriberActions {
        match (self.consumerActions) {
            DurableTopicSubscriberActions c => return c;
            () => {
                error e = {message:"Durable topic subscriber consumerActions cannot be nil."};
                throw e;
            }
        }
    }

    public function stop() {
        self.subscriber.stop();
    }

    public function createTextMessage(string message) returns Message|error {
        var result = self.subscriber.createTextMessage(message);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
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

public type DurableTopicSubscriberActions object {

    public {
        jms:DurableTopicSubscriberActions helper;
    }

    public new(helper) {}

    public function acknowledge(Message message) returns error? {
        return self.helper.acknowledge(message.getJMSMessage());
    }

    public function receive(int timeoutInMilliSeconds = 0) returns Message|error|() {
        var result = self.helper.receive(timeoutInMilliSeconds = timeoutInMilliSeconds);
        match (result) {
            jms:Message m => return new Message(m);
            error e => return e;
            () => return ();
        }
    }
};
