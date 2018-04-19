package ballerina.jms;

import ballerina/log;

public type TopicPublisher object {
    public {
        TopicPublisherConnector connector;
        TopicPublisherEndpointConfiguration config;
    }

    new () {
        self.connector = new ();
    }

    public function init(TopicPublisherEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => self.initTopicPublisher(s);
            () => {}
        }
    }

    public native function initTopicPublisher(Session session);

    public function register (typedesc serviceType) {
    }

    public function start () {
    }

    public function getCallerActions () returns (TopicPublisherConnector) {
        return self.connector;
    }

    public function stop () {
    }
};

public type TopicPublisherEndpointConfiguration {
    Session? session;
    string topicPattern;
};

public type TopicPublisherConnector object {
    public native function send (Message m) returns (Error | ());
};


