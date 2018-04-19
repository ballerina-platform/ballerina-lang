package ballerina.jms;

import ballerina/log;

public type TopicSubscriber object {

    public {
        TopicSubscriberActions consumerActions;
        TopicSubscriberEndpointConfiguration config;
    }

    public function init(TopicSubscriberEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => {
                self.createSubscriber(s, config.messageSelector);
                log:printInfo("Subscriber created for topic " + config.topicPattern);
            }
            () => {}
        }
    }

    public function register(typedesc serviceType) {
        self.registerListener(serviceType, consumerActions);
    }

    native function registerListener(typedesc serviceType, TopicSubscriberActions consumerActions);

    native function createSubscriber(Session session, string messageSelector);

    public function start() {
    }

    public function getCallerActions() returns TopicSubscriberActions {
        return consumerActions;
    }

    public function stop() {
        self.closeSubscriber(consumerActions);
    }

    native function closeSubscriber(TopicSubscriberActions consumerActions);
};

public type TopicSubscriberEndpointConfiguration {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;
};

public type TopicSubscriberActions object {
    public native function acknowledge(Message message) returns error?;

    public native function receive(int timeoutInMilliSeconds = 0) returns Message|error|();
};
