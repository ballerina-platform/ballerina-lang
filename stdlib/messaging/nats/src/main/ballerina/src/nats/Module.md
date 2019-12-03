## Module overview

This module provides the capability to connect with NATS and NATS Streaming servers and performs the 
following functionality.

- Point to point communication (Queues)
- Pub/Sub (Topics)
- Request/Reply

## Basic Usage

### Setting up the connection

First step is setting up the connection with the NATS Basic/Streaming server. The following ways can be used to connect to a
NATS Basic/Streaming server.

1. Connect to a server using the URL
```ballerina
nats:Connection connection = new("nats://localhost:4222");
```

2. Connect to one or more servers with a custom configuration
```ballerina
nats:Connection connection = new("nats://serverone:4222, nats://servertwo:4222",  config);
```

### Publishing messages

Publishing messages is handled differently in the NATS Basic server and Streaming server. The 'ballerina/nats' module provides different 
APIs to publish messages to each server.

#### Publishing messages to the NATS basic server

Once connected, publishing is accomplished via one of the below two methods.

1. Publish with the subject and the message content.
```ballerina
nats:Producer producer = new(connection);
nats:Error? result = producer->publish(subject, "hello world");
```

2. Publish as a request that expects a reply.
```ballerina
nats:Producer producer = new(connection);
nats:Message|nats:Error reqReply = producer->request(subject, "hello world", 5000);
```

3. Publish messages with a replyTo subject 
```ballerina
nats:Producer producer = new(connection);
nats:Error? result = producer->publish(subject, <@untainted>message, 
                         replyToSubject);
```

4. Publish messages with a replyTo callback service
```ballerina
nats:Producer producer = new(connection);
nats:Error? result = producer->publish(subject, <@untainted>message, 
                         replyToService);
```
```ballerina
service replyToService =
@nats:SubscriptionConfig {
    subject: "replySubject"
}
service {

    resource function onMessage(nats:Message msg, string data) {
        // Prints the incoming message in the console.
        log:printInfo("Received reply message : " + data);
    }

    resource function onError(nats:Message msg, nats:Error err) {
        log:printError("Error occurred in data binding", err);
    }
};
```

#### Publishing messages to a NATS streaming server

Once connected to a streaming server, publishing messages is accomplished using the following method.
```ballerina
nats:StreamingProducer producer = new(connection);
string|error result = producer->publish(subject, "hello world");
if (result is error) {
   io:println("Error occurred while publishing the message.");
} else {
   io:println("GUID "+result+" received for the produced message.");
}
```

> Publish api supports the `byte[], boolean, string, int, float, decimal, xml, json, record {}` message types.


### Listening to incoming messages

The Ballerina NATS module provides the following mechanisms to listen to messages. Similar to message publishing, listening to messages
is also handled differently in NATS basic and streaming servers.

#### Listening to messages from a NATS server

```ballerina
import ballerina/io;
import ballerina/nats;

// Initializes the NATS listener.
listener nats:Listener subscription = new(connection);

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:SubscriptionConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:Message msg, string data) {
        // Prints the incoming message in the console.
        io:println("Subject : " + msg.getSubject());
        io:println("Message content : " + data));
    }

    resource function onError(nats:Message msg, nats:Error err) {
        io:println(err);
    }

}
```

#### Listening to messages from a Streaming server

```ballerina
import ballerina/io;
import ballerina/nats;

// Initializes the NATS Streaming listener.
listener nats:StreamingListener subscription = new(conn, "test-cluster", "c1");

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:StreamingMessage msg, string data) {
        // Prints the incoming message in the console.
        io:println("Subject : " + msg.getSubject());
        io:println("Message content : " + data));
    }

    resource function onError(nats:StreamingMessage msg, nats:Error err) {
        io:println(err);
    }

}
```

## Advanced Usage

### Using the TLS protocol

The Ballerina NATS module allows the use of the tls:// protocol in its URLs. This setting expects a secure socket to be 
set in the connection configuration as shown below.

```ballerina
nats:ConnectionConfig config = {
    secureSocket : {
        trustStore : {
            path: "nats-basic/keyStore.p12",
            password: "xxxxx"
        }
    }
};

// Initializes a connection.
nats:Connection connection = new("tls://localhost:4222", config = config);
```
>**Note:** The default thread pool size used in Ballerina is number of processors available * 2. You can configure the thread pool size by using the `BALLERINA_MAX_POOL_SIZE` environment variable.