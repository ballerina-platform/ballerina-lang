package ballerina.jms;

import ballerina/log;

public type TopicSubscriber object {

    public {
        TopicSubscriberConnector connector;
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
        self.registerListener(serviceType, connector);
    }

    native function registerListener(typedesc serviceType, TopicSubscriberConnector connector);

    native function createSubscriber(Session session, string messageSelector);

    public function start() {
    }

    public function getCallerActions() returns TopicSubscriberConnector {
        return connector;
    }

    public function stop() {
        self.closeSubscriber(connector);
    }

    native function closeSubscriber(TopicSubscriberConnector connector);
};

public type TopicSubscriberEndpointConfiguration {
    Session? session;
    string topicPattern;
    string messageSelector;
    string identifier;
};

public type TopicSubscriberConnector object {
    public native function acknowledge(Message message) returns error?;

    public native function receive(int timeoutInMilliSeconds = 0) returns Message|error|();
};
