package ballerina.mb;

import ballerina/jms;

public type Consumer object {

    public function getEndpoint() returns ConsumerTemplate {
        ConsumerTemplate ct = new();
        return ct;
    }
};

public type ConsumerTemplate object {
    public {
        ConsumerConnector connector;
        ConsumerEndpointConfiguration config;
    }

    public function init(ConsumerEndpointConfiguration config) {}

    public function register(typedesc serviceType) {}

    public function start() {}

    public function stop() {}

    public function getCallerActions() returns ConsumerConnector {
        return new;
    }

};

public type ConsumerConnector object {
    public function acknowledge (Message message) returns error? {
        return;
    }
};

public type ConsumerEndpointConfiguration {
    jms:Session? session;
    string identifier;
};
