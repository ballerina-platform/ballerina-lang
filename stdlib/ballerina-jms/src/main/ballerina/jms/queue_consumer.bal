package ballerina.jms;

import ballerina/io;

public struct QueueConsumer {
    QueueConsumerConnector connector;
    QueueConsumerEndpointConfiguration config;
}

public struct QueueConsumerEndpointConfiguration {
    Session session;
    string queueName;
    string identifier;
}

public struct QueueConsumerConnector {
}

public function <QueueConsumer consumer> init(QueueConsumerEndpointConfiguration config) {

    consumer.config = config;
    SessionConnector sessionConnector = config.session.getClient();
    consumer.createConsumer(sessionConnector);
    io:println("Consumer created for queue " + config.queueName);
}

public function <QueueConsumer consumer> register (typedesc serviceType) {
    QueueConsumerConnector connector = consumer.connector;
    consumer.registerListener(serviceType, connector);
}

native function <QueueConsumer consumer> registerListener(typedesc serviceType, QueueConsumerConnector connector);

native function <QueueConsumer consumer> createConsumer (SessionConnector connector);

public function <QueueConsumer consumer> start () {
}

public function <QueueConsumer consumer> getClient () returns (QueueConsumerConnector) {
    return consumer.connector;
}

public function <QueueConsumer consumer> stop () {
}

