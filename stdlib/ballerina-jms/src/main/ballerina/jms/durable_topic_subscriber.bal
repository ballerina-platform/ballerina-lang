package ballerina.jms;

import ballerina/log;

public type DurableTopicSubscriber object {

    public {
        DurableTopicSubscriberActions consumerActions;
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

    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    native function registerListener(typedesc serviceType, DurableTopicSubscriberActions consumerActions);

    native function createSubscriber(Session session, string messageSelector);

    public function start() {
    }

    public function getCallerActions() returns DurableTopicSubscriberActions {
        return consumerActions;
    }

    public function stop() {
        self.closeSubscriber(consumerActions);
    }

    native function closeSubscriber(DurableTopicSubscriberActions consumerActions);
};

public type DurableTopicSubscriberEndpointConfiguration {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;

};

public type DurableTopicSubscriberActions object {
    public native function acknowledge(Message message) returns error?;

    public native function receive(int timeoutInMilliSeconds = 0) returns Message|error|();
};
