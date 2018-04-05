package ballerina.jms;

public struct Consumer {
}

public struct ConsumerTemplate {
    ConsumerConnector connector;
    ConsumerEndpointConfiguration config;
}

public struct ConsumerConnector {
}

public struct ConsumerEndpointConfiguration {
    Session session;
    string identifier;
}

public function <ConsumerTemplate c> init(ConsumerEndpointConfiguration config) {}

public function <ConsumerTemplate c> register(typedesc serviceType) {}

public function <ConsumerTemplate c> start() {}

public function <ConsumerTemplate c> stop() {}

public function <ConsumerTemplate c> getClient() returns (ConsumerConnector) {
    return c.connector;
}

function <Consumer c> getEndpoint() returns ConsumerTemplate {
    ConsumerTemplate ct = {};
    return ct;
}

