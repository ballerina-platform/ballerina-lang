## Module overview

The `ballerina/jms` module provides an API to connect to an external JMS provider like Ballerina Message Broker or
ActiveMQ.

The module provides consumer and producer endpoint types for queues and topics. Following are the endpoint types
supported by this module:

- QueueReceiver
- TopicSubscriber
- DurableTopicSubscriber
- SimpleQueueReceiver
- SimpleTopicSubscriber
- SimpleDurableTopicSubscriber
- QueueSender
- TopicPublisher
- SimpleQueueSender
- SimpleTopicPublisher

The endpoints prefixed with `Simple` will automatically create a JMS connection and a JMS session when the endpoint is
initialized. For other endpoints, the developer must explicitly provide a properly initialized JMS Session.

## Samples

### JMS Simple Queue Consumer

Following is a simple listener program that consumes messages from a Ballerina Message Broker queue named `MyQueue`.

```ballerina
import ballerina/jms;
import ballerina/log;

// Create a simple queue receiver.
listener jms:SimpleQueueReceiver consumerEP = new({
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
});

// Bind the created consumer to the listener service.
service jmsListener on consumerEP {

    // The `OnMessage` resource gets invoked when a message is received.
    var msg = message.getTextMessageContent();
    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
        if (msg is string) {
            log:printInfo("Message : " + msg);
        } else {
            log:printError("Error occurred while reading message", err = msg);
        }
    }
}
```
### JMS Simple Queue Producer.

Following is a simple queue sender program that sends messages to a Ballerina Message Broker queue named `MyQueue`.

```ballerina
import ballerina/jms;
import ballerina/log;

// Create a topic publisher.
jms:SimpleTopicPublisher topicPublisher = new({
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic"
});

public function main(string... args) {
    // Create a text message.
    var msg = topicPublisher.createTextMessage("Hello from Ballerina");
    if (msg is error) {
        log:printError("Error occurred while creating message", err = msg);
    } else {
        var result = topicPublisher->send(msg);
        if (result is error) {
            log:printError("Error occurred while sending message", err = result)
        };
    }
}
```

### JMS Queue Message Receiver

Following is a listener program that explicitly initializes a JMS session to be used in the consumer.

```ballerina
import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection conn = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
        // An optional property that defaults to `AUTO_ACKNOWLEDGE`.
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a queue receiver using the created session.
listener jms:QueueReceiver consumerEP = new({
    session: jmsSession,
    queueName: "MyQueue"
});

// Bind the created consumer to the listener service.
service jmsListener on consumerEP {

    // The `OnMessage` resource gets invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
        // Retrieve the text message.
        var msg = message.getTextMessageContent();
        if (msg is string) {
            log:printInfo("Message : " + message.getTextMessageContent());
        } else {
            log:printError("Error occurred while reading message", err = msg);
        }
    }
}
```

### JMS Queue Message Producer

Following is a queue sender program that explicitly initializes a JMS session to be used in the producer.


```ballerina
import ballerina/jms;
import ballerina/log;

// Initialize a JMS connection with the provider.
jms:Connection jmsConnection = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@ballerina/default?brokerlist='tcp://localhost:5672'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

jms:QueueSender queueSender = new({
    session: jmsSession,
    queueName: "MyQueue"
});

public function main(string... args) {
    // Create a text message.
    var msg = jmsSession.createTextMessage("Hello from Ballerina");
    if (msg is error) {
        log:printError("Error occurred while creating message", err = msg);
    } else {
        var result = queueSender->send(msg);
        if (result is error) {
            log:printError("Error occurred while sending message", err = result)
        }
    }
}
```

### JMS Topic Subscriber

Following is a topic subscriber program that subscribes to a particular JMS topic.

```ballerina
import ballerina/jms;
import ballerina/log;

jms:Connection conn = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

jms:Session jmsSession = new(conn, {
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
});

listener jms:TopicSubscriber subscriberEndpoint = new({
    session:jmsSession,
    topicPattern:"BallerinaTopic"
});

service jmsListener on subscriberEndpoint {
    onMessage(jms:TopicSubscriberCaller subscriber, jms:Message message) {
        var msg = message.getTextMessageContent();
        if (msg is string) {
            log:printInfo("Message : " + msg);
        } else {
            log:printError("Error occurred while reading message", err = msg);
        }
    }
}
```

### JMS Topic Producer

Following is a topic producer program that publishes to a particular JMS topic.

```ballerina
import ballerina/jms;
import ballerina/log;

jms:Connection jmsConnection = new({
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
});

jms:Session jmsSession = new(jmsConnection, {
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
});

jms:TopicPublisher topicPublisher = new({
    session:jmsSession,
    topicPattern:"BallerinaTopic"
});

public function main(string... args) {
    var msg = jmsSession.createTextMessage("Hello from Ballerina");
    if (msg is error) {
        log:printError("Error occurred while creating message", err = msg);
    } else {
        var result = topicPublisher->send(msg);
        if (result is error) {
            log:printError("Error occurred while sending message", err = result)
        }
    }
}
```
