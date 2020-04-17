## Module Overview

This module provides support for the gRPC messaging protocol. gRPC is an inter-process communication technology that allows you to connect, invoke and operate distributed heterogeneous applications as easily as making a local function call. The gRPC protocol is layered over HTTP/2 and It uses Protocol Buffers for marshaling/unmarshaling messages. This makes gRPC, highly efficient on wire and a simple service definition framework.

When you develop a gRPC application the first thing you do is define a service definition using Protocol Buffers.

### Protocol buffers
This is a mechanism to serialize the structured data introduced by Google and used by the gRPC framework. Defining the service using Protocol Buffers includes defining remote methods in the service and defining message types that are sent across the network. A sample service definition is shown below.

```proto
syntax = "proto3";

service Helloworld {
     rpc hello(HelloRequest) returns (HelloResponse);
}

message HelloRequest {
  string name = 1;
}

message HelloResponse {
  string message = 1;
}
```

gRPC allows client applications to directly call the server-side methods using the auto-generated stubs. Protocol Buffer compiler is used to generate the stubs for the specified language. In Ballerina, the stubs are generated using the built-in proto compiler. 

For the guide on how to generate Ballerina code for Protocol Buffers definition, see [how to guide](https://ballerina.io/learn/how-to-generate-code-for-protocol-buffers/).
For examples on the usage of the operation, see the [Proto to Ballerina Example](https://ballerina.io/learn/by-example/proto-to-ballerina.html).

### gRPC Communication Patterns
The common communication pattern between client and server is simple request-response style communication. However, with gRPC, you can leverage different inter-process communication patterns other than the simple request-response pattern.
This module supports four fundamental communication patterns used in gRPC-based applications: simple RPC(unary RPC), server-side streaming, client-side streaming, and bidirectional streaming.

#### Simple RPC (Unary RPC) 
In this pattern, the client invokes a remote function of a server and sends a single request to the server. The server sends a single response in return to the client along with status details.
Users can invoke in both blocking and non-blocking manner.

```proto
rpc hello (google.protobuf.StringValue)
      returns (google.protobuf.StringValue);
```
##### Creating the server
The code snippet given below contains a service that sends a response to each request.

```ballerina
// The gRPC service is attached to the listener.
service HelloWorld on new grpc:Listener(9090)  {
   // A resource that accepts a string message.
   resource function hello(grpc:Caller caller, string name) {
       // Send the response to the client.
       grpc:Error? err = caller->send("Hi " + name + "! Greetings from gRPC service!");

       // After sending the response, call ‘complete()’ to indicate that the response was completely sent.
       grpc:Error? result = caller->complete();
   }
}
```
##### Creating the client
The code snippet given below calls the above service in a blocking manner using an auto-generated Ballerina stub.

```ballerina
// Use ‘BlockingClient’ to execute the call in the blocking mode.
HelloWorldBlockingClient blockingClient = new("http://localhost:9090");

// Create gRPC headers.
grpc:Headers headers = new;
headers.setEntry("id", "newrequest1");

// Call the method in the service using a client stub.
[string, grpc:Headers]|grpc:Error responseFromServer = blockingClient->hello("Ballerina", headers);
```
For examples on the usage of the operation, see [Unary Ballerina Example](https://ballerina.io/learn/by-example/grpc-unary-blocking.html) and [Unary Non-Ballerina Example](https://ballerina.io/learn/by-example/grpc-unary-non-blocking.html)

#### Server streaming RPC
In server-side streaming RPC, the sends back a sequence of responses after getting the client's request message. After sending all the server responses, the server marks the end of the stream by sending the server status details.
Users can invoke in a non-blocking manner.

```proto
rpc lotsOfReplies (google.protobuf.StringValue)
      returns (stream google.protobuf.StringValue);
```
##### Creating the server
The code snippet given below contains a service that sends a sequence of responses to each request.

```ballerina
// The gRPC service is attached to the listener.
service HelloWorld on new grpc:Listener(9090) {
   // Set the Streaming Annotation to ‘true’. It specifies that the resource is capable of
   // sending multiple responses per request.
   @grpc:ResourceConfig { streaming: true }
   resource function lotsOfReplies(grpc:Caller caller, string name) {
       string[] greets = ["Hi", "Welcome"];
       // Send multiple responses to the client.
       foreach string greet in greets {
           grpc:Error? err = caller->send(greet + " " + name + "! Greetings from Ballerina service");
       }
       // Once the messages are sent to the server, call ‘complete()’ to indicate that the request was completely sent.
       grpc:Error? result = caller->complete();
   }
}
```

##### Creating the client
The code snippet given below calls the above service using the auto-generated Ballerina client stub and listens to the multiple responses from the server.

```ballerina
   // Client endpoint configurations.
    HelloWorldClient helloworldClient = new("http://localhost:9090");

    // Execute the service streaming call by registering a message listener.
    grpc:Error? result = helloworldClient->lotsOfReplies
("Ballerina", HelloWorldMessageListener);

// Define a listener service to receive the messages from the server.
service HelloWorldMessageListener = service {

   // This resource method is invoked when the service receives a message.
   resource function onMessage(string message) {
   }
   
   // This resource method is invoked if an error is returned.
   resource function onError(error err) {
   }

   // Invoke this resource when the server sends all the responses to the request.
    resource function onComplete() {
   }
}
```
For examples on the usage of the operation, see the [Server Streaming Example](https://ballerina.io/learn/by-example/grpc-server-streaming.html).

#### Client streaming RPC
In client streaming RPC, the client sends multiple messages to the server instead of a single request. The server sends back a single response to the client.

```proto
rpc lotsOfGreetings (stream google.protobuf.StringValue)
      returns (google.protobuf.StringValue);
```

##### Creating the server
The code snippet given below contains a service that receives a sequence of requests from the client and a single response in return.

```ballerina
// The gRPC service is attached to the listener.
@grpc:ServiceConfig {
    name: "HelloWorld",
    clientStreaming: true
}
service HelloWorld on new grpc:Listener(9090) {
//This `resource` is triggered when a new caller connection is initialized.
resource function onOpen(grpc:Caller caller) {
}

//This `resource` is triggered when the caller sends a request message to the service.
resource function onMessage(grpc:Caller caller, string name) {
}

//This `resource` is triggered when the server receives an error message from the caller.
resource function onError(grpc:Caller caller, error err) {
}

//This `resource` is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
resource function onComplete(grpc:Caller caller) {
    grpc:Error? err = caller->send("Ack");
}
}
```

##### Creating the client
The code snippet given below calls the above service using the auto-generated Ballerina client stub and sends multiple request messages from the server.

```ballerina
   // Client endpoint configurations.
    HelloWorldClient helloworldClient = new("http://localhost:9090");

    // Execute the service streaming call by registering a message listener.
    grpc:StreamingClient|grpc:Error ep = helloworldClient->lotsOfGreetings(HelloWorldMessageListener);

// Send multiple messages to the server.
string[] greets = ["Hi", "Hey", "GM"];
foreach string greet in greets {
    grpc:Error? connErr = ep->send(greet + " " + "Ballerina");
}

// Once all the messages are sent, the server notifies the caller with a `complete` message.
grpc:Error? result = ep->complete();
...

// Define a listener service to receive the messages from the server.
service HelloWorldMessageListener = service {

   // This resource method is invoked when the service receives a message.
   resource function onMessage(string message) {
   }
   
   // This resource method is invoked if an error is returned.
   resource function onError(error err) {
   }

   // Invoke this resource when the server sends all the responses to the request.
    resource function onComplete() {
   }
}
```
For examples on the usage of the operation, see the [Client Streaming Example](https://ballerina.io/learn/by-example/grpc-client-streaming.html).

#### Bidirectional Streaming RPC
In bidirectional streaming RPC, the client is sending a request to the server as a stream of messages. The server also responds with a stream of messages.

```proto
rpc chat (stream ChatMessage)
      returns (stream google.protobuf.StringValue);
```
##### Creating the server
The code snippet given below includes a service that handles bidirectional streaming.

```ballerina
// Set the ‘clientStreaming’ and ‘serverStreaming’ to true. It specifies that the service supports bidirectional streaming.
@grpc:ServiceConfig {
    name: "Chat",
    clientStreaming: true,
    serverStreaming: true
}
service Chat on new grpc:Listener(9090) {

    //This `resource` is triggered when a new caller connection is initialized.
    resource function onOpen(grpc:Caller caller) {
    }

    //This `resource` is triggered when the caller sends a request message to the `service`.
    resource function onMessage(grpc:Caller caller, ChatMessage chatMsg) {
            grpc:Error? err = caller->send(string `${chatMsg.name}: ${chatMsg.message}`);
    }

    //This `resource` is triggered when the server receives an error message from the caller.
    resource function onError(grpc:Caller caller, error err) {
    }

    //This `resource` is triggered when the caller sends a notification to the server to indicate that it has finished sending messages.
    resource function onComplete(grpc:Caller caller) {
    }
}
```
##### Creating the client
The code snippet given below calls the above service using the auto-generated Ballerina client stub and sends multiple request messages to the server and receives multiple responses from the server.

```ballerina
   // Client endpoint configurations.
    ChatClient chatClient = new("http://localhost:9090");

    // Execute the service streaming call by registering a message listener.
    grpc:StreamingClient|grpc:Error ep = = chatClient->chat(ChatMessageListener);

// Send multiple messages to the server.
string[] greets = ["Hi", "Hey", "GM"];
foreach string greet in greets {
    ChatMessage mes = {name: "Ballerina", message: greet};
    grpc:Error? connErr = ep->send(mes);
}

// Once all the messages are sent, the server notifies the caller with a `complete` message.
grpc:Error? result = ep->complete();
...

// Define a listener service to receive the messages from the server.
service ChatMessageListener = service {

   // This resource method is invoked when the service receives a message.
   resource function onMessage(string message) {
   }
   
   // This resource method is invoked if an error is returned.
   resource function onError(error err) {
   }

   // Invoke this resource when the server sends all the responses to the request.
    resource function onComplete() {
   }
}
```
For examples on the usage of the operation, see the [Bidirectional Streaming Example](https://ballerina.io/learn/by-example/grpc-bidirectional-streaming.html).

### Advanced Use cases

#### Using the TLS protocol

The Ballerina gRPC module allows the use TLS in communication. This setting expects a secure socket to be 
set in the connection configuration as shown below.

##### Configuring TLS in server side

```ballerina
// Server endpoint configuration with the SSL configurations.
listener grpc:Listener ep = new (9090, {
    host: "localhost",
    secureSocket: {
        keyStore: {
            path: config:getAsString("b7a.home") + "bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service HelloWorld on ep {
    
}
```

##### Configuring TLS in client side

```ballerina
    // Client endpoint configuration with SSL configurations.
    HelloWorldBlockingClient helloWorldClient = new ("https://localhost:9090", {
            secureSocket: {
                trustStore: {
                    path: config:getAsString("b7a.home") + "/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                }
            }
    });
```

For examples on the usage of the operation, see the [Secured Unary Example](https://ballerina.io/learn/by-example/grpc-secured-unary.html).
