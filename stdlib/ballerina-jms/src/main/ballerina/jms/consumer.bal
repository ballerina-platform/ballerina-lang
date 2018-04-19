package ballerina.jms;

public type Consumer object {
    public function getEndpoint() returns ConsumerTemplate {
        ConsumerTemplate ct = new;
        return ct;
    }
};

public type ConsumerTemplate object {
    public {
        ConsumerActions consumerActions;
        ConsumerEndpointConfiguration config;
    }

    public function init(ConsumerEndpointConfiguration config) {}

    public function register(typedesc serviceType) {}

    public function start() {}

    public function stop() {}

    public function getCallerActions() returns ConsumerActions {
        return new;
    }

};

public type ConsumerActions object {
    public function acknowledge(Message message) returns error? {
        return;
    }
};

public type ConsumerEndpointConfiguration {
    Session? session;
    string identifier;
};
