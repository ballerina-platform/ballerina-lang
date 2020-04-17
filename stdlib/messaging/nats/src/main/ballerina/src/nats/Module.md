## Module Overview

This module provides the capability to connect with NATS and NATS Streaming servers and performs the 
below functionalities.

- Point to point communication (Queues)
- Pub/Sub (Topics)
- Request/Reply

### Basic Usage

#### Setting up the connection

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

#### Publishing messages

Publishing messages is handled differently in the NATS Basic server and Streaming server. The 'ballerina/nats' module provides different 
APIs to publish messages to each server.

##### Publishing messages to the NATS basic server

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
    }

    resource function onError(nats:Message msg, nats:Error err) {
    }
};
```

##### Publishing messages to a NATS streaming server

Once connected to a streaming server, publishing messages is accomplished using the following method.
```ballerina
nats:StreamingProducer producer = new(connection);
string|error result = producer->publish(subject, "hello world");
```

> Publish api supports the `byte[], boolean, string, int, float, decimal, xml, json, record {}` message types.


#### Listening to incoming messages

The Ballerina NATS module provides the following mechanisms to listen to messages. Similar to message publishing, listening to messages
is also handled differently in the NATS basic and streaming servers.

##### Listening to messages from a NATS server

```ballerina
// Initializes the NATS listener.
listener nats:Listener subscription = new(connection);

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:SubscriptionConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:Message msg, string data) {
    }

    resource function onError(nats:Message msg, nats:Error err) {
    }
}
```

##### Listening to messages from a Streaming server

```ballerina
// Initializes the NATS Streaming listener.
listener nats:StreamingListener subscription = new(conn, "test-cluster", "c1");

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:StreamingMessage msg, string data) {
    }

    resource function onError(nats:StreamingMessage msg, nats:Error err) {
    }

}
```

### Advanced Usage

#### Using the TLS protocol

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
>**Note:** The default thread pool size used in Ballerina is the number of processors available * 2. You can configure the thread pool size by using the `BALLERINA_MAX_POOL_SIZE` environment variable.

For information on the operations, which you can perform with this module, see the below **Functions**. 

For examples on the usage of the connector, see the following.
* [Basic Publisher and Subscriber Example](https://ballerina.io/learn/by-example/nats-basic-client.html).
* [Basic Streaming Publisher and Subscriber Example](https://ballerina.io/learn/by-example/nats-streaming-client.html)
* [Streaming Publisher and Subscriber With Data Binding Example](https://ballerina.io/learn/by-example/nats-streaming-consumer-with-data-binding.html)
* [Durable Subscriptions Example](https://ballerina.io/learn/by-example/nats-streaming-durable-subscriptions.html)
* [Queue Groups Example](https://ballerina.io/learn/by-example/nats-streaming-queue-group.html)
* [Historical Message Replay Example](https://ballerina.io/learn/by-example/nats-streaming-start-position.html)
