## Module overview

NATS is a simple, secure and high performance open source messaging system for cloud native applications.
'ballerina/nats' provides the capability to connect with a NATS and Streaming server and perform the following

- Point to point communication (Queues)
- Pub/Sub (Topics)
- Request/Reply

## Basic Usage

### Connecting

First step is setting up connection with NATS/Streaming server. Following ways can be used to connect to 
NATS/Streaming server.

1. Connect to a server using a URL
```ballerina
nats:Connection connection = new("nats://localhost:4222");
```

2. Connect to one or more servers with a custom configuration
```ballerina
nats:Connection connection = new("nats://serverone:4222,nats://servertwo:4222",  config = config);
```

### Publishing

Publishing messages is handled differently in NATS server and Streaming server. 'ballerina/nats' provides different 
apis to publish messages to each server.

#### NATS Server

Once connected, publishing is accomplished via one of three methods.

1. Publish with subject and message content
```ballerina
nats:Producer producer = new(connection);
var result = producer->publish(subject, "hello world");
```

2. Publish with subject, message content and a subject for the receiver to replyTo.
```ballerina
nats:Producer producer = new(connection);
var result = producer->publish(subject, "hello world", replyTo = "replyTo");
```

3. Publish as a request that expects a reply.
```ballerina
nats:Producer producer = new(connection);
var reqReply = producer->request(subject, "hello world", 5000);
```

#### Streaming Server

Once connected to streaming server, publishing message is accomplished using following method.
```ballerina
nats:StreamingProducer producer = new(connection);
var result = producer->publish(subject, "hello world");
if (result is error) {
   io:println("Error occurred while publishing the message.");
} else {
   io:println("GUID "+result+" received for the produced message.");
}
```

NOTE: Publish api supports `byte[], boolean, string, int, float, decimal, xml, json, record {}` message types.


### Listening for Incoming Messages

The Ballerina NATS module provides following mechanisms to listen for messages. Like message publishing, listening 
also handles differently in NATS server and Streaming server.

#### NATS Server

```ballerina
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

    resource function onError(nats:Message msg, nats:NatsError err) {
        io:println(err);
    }

}
```

#### Streaming Server

```ballerina
// Initializes the NATS listener.
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

    resource function onError(nats:StreamingMessage msg, nats:NatsError err) {
        io:println(err);
    }

}
```

## Advanced Usage

### TLS

The Ballerina NATS module allows the use of the tls:// protocol in its urls. This setting expects secureSocket to be 
set in connection configuration like below.

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