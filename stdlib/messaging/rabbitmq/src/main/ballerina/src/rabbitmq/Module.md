## Module overview

[RabbitMQ](https://www.rabbitmq.com/) is one of the most popular open-source enterprise messaging systems, modeled on the Advanced Message Queuing Protocol (AMQP) standard. This guide covers Ballerina RabbitMQ module and its public API. This module contains functionality to support messaging with RabbitMQ broker (AMQP 0-9-1). It assumes that the most recent version of Ballerina is used and the reader is familiar with the basics.
 
Key sections include: 
- Connecting to RabbitMQ
- Using Exchanges and Queues
- Publishing Messages
- Consuming Messages using Consumer Services
- Client Acknowledgements 

### Connecting to RabbitMQ

The core APIs are `Connection`, `Channel` and `Message`, representing an AMQP 0-9-1 Connection, Channel and a Ballerina RabbitMQ Message, respectively. The following code connects to a RabbitMQ node using the given parameters (host name, port number, etc): 

```ballerina
   rabbitmq:Connection newConnection = new({ host: "localhost", 
                                            port: 5672, 
                                            username: “guest”, 
                                            password: “guest” });
```

The `Connection` created above can then be used to open a `Channel`: 

```ballerina
   rabbitmq:Channel newChannel = new(connection);
```

The `Channel` can now be used to send and receive messages, as described in subsequent sections. 

#### Enabling TLS

It is possible to encrypt the communication between the Ballerina client and the broker by using TLS enabled connections. Client and server authentication (peer verification) is also supported. To enable the TLS support in the RabbitMQ broker, the node has to be [configured](https://www.rabbitmq.com/ssl.html#enabling-tls) to know the location of the Certificate Authority bundle, the server's certificate file, and the server's key. A TLS listener should also be configured to know what port to listen on for TLS-enabled client connections.

Connecting to a TLS enabled RabbitMQ node using the Ballerina client can be done by passing a `SecureSocket` record with the appropriate values, to the `ConnectionConfiguration` record when initializing the connection.  

```ballerina
   rabbitmq:Connection connection = new({ host: "localhost", 
                                    	    port: 5671, 
                                            secureSocket: { trustStore: { path: "/path/to/trustStore",
                                                                        password: "rabbitstore" },
                                                          keystore: { path: "/path/to/client_key.p12",
                                                                      password: "MySecretPassword" },
                                                          verifyHostname: true }});
```

#### Disconnecting from RabbitMQ

To disconnect, simply close the open channels and the connections: 

```ballerina
   newChannel.close();
   newConnection.close();
```
> Note: Closing the `Channel` may be considered good practice, but isn’t strictly necessary here; it will be done automatically anyway when the underlying `Connection` is closed. 

### Using Exchanges and Queues

Client applications work with exchanges and queues which are the high-level building blocks of the AMQP protocol. These must be declared before they can be used. The following code declares an exchange and a server-named queue, then binds them together. 

```ballerina
   var exchangeResult = newChannel->exchangeDeclare({ exchangeName: "MyExchange",
                                                      exchangeType: rabbitmq:DIRECT_EXCHANGE,
                                                      durable: true,
                                                      autoDelete: true });
   if (exchangeResult is error) {
        io:println("An error occurred while declaring the exchange");
   }
   
   var queueResult = newChannel->queueDeclare();
   if (queueResult is string) {
   
        var bindResult = newChannel.queueBind(queueResult, "MyExchange", "routing-key");
        if (bindResult is error) {
            io:println("Error ocurred while binding the queue to the exhchange");
        }
   }
   else {
        io:println("An error occurred while creating the queue.");
   } 
```

This sample code will declare,
- A durable, auto-delete exchange of type rabbitmq:DIRECT_EXCHANGE
- A non-durable, exclusive, auto-delete queue with an auto-generated name

The above function calls then bind the queue to the exchange with the given routing key. 

```ballerina
   var exchangeResult = newChannel->exchangeDeclare({ exchangeName: "MyExchange",
                                                      exchangeType: rabbitmq:DIRECT_EXCHANGE,
                                                      durable: true,
                                                      autoDelete: true });
   if (exchangeResult is error) {
        io:println("An error occurred while declaring the exchange");
   }
   
   var queueResult = newChannel->queueDeclare({ queueName: "MyQueue", 
                                                durable: true,
                                                exclusive: false,
                                                autoDelete: false });
   if (queueResult is error) {
    io:println("An error occurred while creating the MyQueue queue.");
   }

    var bindResult = newChannel.queueBind("MyQueue", "MyExchange", "routing-key");
        if (bindResult is error) {
            io:println("Error ocurred while binding the queue to the exhchange");
        }
```

This sample code will declare,
 - A durable, auto-delete exchange of type rabbitmq:DIRECT_EXCHANGE
 - A durable, non-exclusive, non-auto-delete queue with a well-known name

#### Deleting entities and purging queues

- Delete a queue
```ballerina
   newChannel->queueDelete("MyQueue");
```
- Delete a queue only if it is empty
```ballerina
   newChannel->queueDelete("MyQueue", false, true);
```
- Delete a queue only if it is unused (does not have any consumers)
```ballerina
   newChannel->queueDelete("MyQueue", true, false);
```
- Delete an exchange
```ballerina
   newChannel->exchangeDelete("MyExchange");
```
- A queue can be purged (all of its messages deleted):
```ballerina
   newChannel->queuePurge("MyQueue");
```

### Publishing Messages

To publish a message to an exchange, use the basicPublish() function as follows:

```ballerina
   var sendResult = newChannel->basicPublish("Hello from Ballerina", "MyQueue");
   if (sendResult is error) {
        io:println("An error occurred while sending the message to MyQueue using newChannel.");
   }
``` 
Setting other properties of the message such as routing headers can be done by using the `BasicProperties` record with appropriate values. 

### Consuming Messages using Consumer Services

The most efficient way to receive messages is to set up a subscription using a Ballerina RabbitMQ `Listener` and any number of consumer services. The messages will then be delivered automatically as they arrive, rather than having to be explicitly requested. 

Multiple consumer services can be bound to one Ballerina RabbitMQ `Listener`. The queue which the service is listening to, is configured in the `ServiceConfig` annotation of the service. 

```ballerina
listener rabbitmq:Listener channelListener= new(newConnection);

@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    }
}
service rabbitmqConsumer on channelListener {
    resource function onMessage(rabbitmq:Message message) {
        var messageContent = message.getTextContent();
        if (messageContent is string) {
            log:printInfo("The message received: " + messageContent);
        } else {
            log:printError("Error occurred while retrieving the message content.");
        }
    }
}
```
The `Message` object received can be used to retrieve its contents and for manual client acknowledgements. 

### Client Acknowledgements

The message consuming is supported by mainly two types of acknowledgement modes; which are auto acknowledgements and client acknowledgements. 
Client acknowledgements can further be divided to two different types as positive and negative acknowledgements. 
The acknowledgement mode is by default auto-ack (messages are acknowledged immediately after consuming).
> WARNING: To ensure the reliability of receiving messages, use the client-ack mode. 

The negatively acknowledged (rejected) messages can be re-queued. 


>**Note:** The default thread pool size used in Ballerina is number of processors available * 2. You can configure the thread pool size by using the `BALLERINA_MAX_POOL_SIZE` environment variable.
