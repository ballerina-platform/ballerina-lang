package ballerina.jms;

import ballerina/log;

public type DurableTopicSubscriber object {

    public {
        DurableTopicSubscriberConnector connector;
        DurableTopicSubscriberEndpointConfiguration config;
    }

    public function init(DurableTopicSubscriberEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => {
                self.createSubscriber(s, config.messageSelector);
                log:printInfo("Durable subscriber created for topic " + config.topicPattern);
            }
            () => {}
        }
    }

    public function register (typedesc serviceType) {
        self.registerListener(serviceType, connector);
    }

    native function registerListener(typedesc serviceType, DurableTopicSubscriberConnector connector);

    native function createSubscriber (Session session, string messageSelector);

    public function start() {
    }

    public function getCallerActions () returns (DurableTopicSubscriberConnector) {
        return connector;
    }

    public function stop() {
        self.closeSubscriber(connector);
    }

    native function closeSubscriber(DurableTopicSubscriberConnector connector);
};

public type DurableTopicSubscriberEndpointConfiguration {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;

};

public type DurableTopicSubscriberConnector object {
    public native function acknowledge (Message message) returns error?;

    public native function receive (int timeoutInMilliSeconds = 0) returns Message|error|();
};
