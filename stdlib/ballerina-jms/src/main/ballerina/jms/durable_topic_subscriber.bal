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
                createSubscriber(s);
                log:printInfo("Durable subscriber created for topic " + config.topicPattern);
            }
            () => {}
        }
    }

    public function register (typedesc serviceType) {
        registerListener(serviceType, connector);
    }

    native function registerListener(typedesc serviceType, DurableTopicSubscriberConnector connector);

    native function createSubscriber (Session session);

    public function start () {
    }

    public function getClient () returns (DurableTopicSubscriberConnector) {
        return connector;
    }

    public function stop () {
        closeSubscriber(connector);
    }

    native function closeSubscriber(DurableTopicSubscriberConnector connector);
};

public type DurableTopicSubscriberEndpointConfiguration {
    Session? session;
    string topicPattern;
    string identifier;
};

public type DurableTopicSubscriberConnector object {
    public native function acknowledge (Message message);

    public native function receive (int timeoutInMilliSeconds = 0) returns (Message | ());
};
