package ballerina.jms;

import ballerina/log;

public type TopicProducer object {
    public {
        TopicProducerConnector connector;
        TopicProducerEndpointConfiguration config;
    }

    new () {
        self.connector = new ();
    }

    public function init(TopicProducerEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => self.initTopicProducer(s);
            () => {}
        }
    }

    public native function initTopicProducer(Session session);

    public function register (typedesc serviceType) {
    }

    public function start () {
    }

    public function getClient () returns (TopicProducerConnector) {
        return self.connector;
    }

    public function stop () {
    }
};

public type TopicProducerEndpointConfiguration {
    Session? session;
    string topicPattern;
};

public type TopicProducerConnector object {
    public native function send (Message m);
};


