## Module overview

The `ballerina/mb` module provides an API to connect to a Ballerina Message Broker instance.

The module provides consumer and producer endpoint types for queues and topics. Following are the endpoint types
supported by this module:

- SimpleQueueReceiver
- SimpleTopicSubscriber
- SimpleDurableTopicSubscriber
- SimpleQueueSender
- SimpleTopicPublisher

## Samples

### Simple Queue Receiver

Following is a simple listener program that consumes messages from a Ballerina Message Broker queue named `MyQueue`.

```ballerina
import ballerina/mb;
import ballerina/log;

// Create a simple queue receiver.
endpoint mb:SimpleQueueReceiver receiver {
   host: "localhost",
   port: 5672,
   queueName: "MyQueue"
};

// Bind the receiver to the queue to get the messages.
service<mb:Consumer> mbListener bind receiver {

   // Receive the messages that come to the queue.
   onMessage(endpoint consumer, mb:Message message) {
       match (message.getTextMessageContent()) {
           string messageText => log:printInfo("Message : " + messageText);
           error e => log:printError("Error occurred while reading message", err=e);
       }
   }
}
```

### Simple Queue Sender

Following is a simple queue sender program that sends messages to a Ballerina Message Broker queue named `MyQueue`.

```ballerina
import ballerina/mb;
import ballerina/log;

// Create a queue sender.
endpoint mb:SimpleQueueSender queueSender {
   host: "localhost",
   port: 5672,
   queueName: "MyQueue"
};

public function main (string... args) {
   // Create a text message.
   match (queueSender.createTextMessage("Hello from Ballerina")) {
       error e => {
           log:printError("Error occurred while creating message", err = e);
       }

       mb:Message msg => {
           // Send the Ballerina message to the JMS provider.
           queueSender->send(msg) but { error e => log:printError("Error occurred while sending message", err = e) };
       }
   }
}
```
