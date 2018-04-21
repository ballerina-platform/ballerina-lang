
import ballerina/log;

public type TopicPublisher object {
    public {
        TopicPublisherActions producerActions;
        TopicPublisherEndpointConfiguration config;
    }

    new () {
        self.producerActions = new;
    }

    public function init(TopicPublisherEndpointConfiguration config) {
        self.config = config;
        match (config.session) {
            Session s => self.initTopicPublisher(s);
            () => {}
        }
    }

    public native function initTopicPublisher(Session session);

    public function register(typedesc serviceType) {
    }

    public function start() {
    }

    public function getCallerActions() returns TopicPublisherActions {
        return self.producerActions;
    }

    public function stop() {
    }
};

public type TopicPublisherEndpointConfiguration {
    Session? session;
    string topicPattern;
};

public type TopicPublisherActions object {
    public native function send (Message m) returns error?;
};
