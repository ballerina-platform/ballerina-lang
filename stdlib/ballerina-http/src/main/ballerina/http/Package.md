## Package overview

This package provides an implementation for connecting and interacting with HTTP, HTTP2, and WebSocket endpoints. The package facilitates two types of endpoints as ‘Client’ and ‘Listener’. 

### Client endpoints

`Client` endpoints are used to connect to and interact with HTTP endpoints. They support connection pooling and can be configured to have a maximum number of active connections that can be made with the remote endpoint. `Client` endpoints activate connection eviction after a given idle period and also support follow-redirects so that the users do not have to manually handle 3xx HTTP status codes. 

`Client` endpoints handle resilience in multiple ways such as load balancing, circuit breaking, endpoint timeouts, and a retry mechanism.

Load balancing is used in the round robin or failover manner. 

When a failure occurs in the remote service, the client connections might wait for some time before a timeout occurs. Awaiting requests consume resources in the system. Circuit Breakers are used to trip after a certain number of failed requests to the remote service. Once a circuit breaker trips, it does not allow the client to send requests to the remote service for a period of time.

The Ballerina circuit breaker supports tripping on HTTP error status codes and I/O errors. Failure thresholds can be configured based on a sliding window (e.g., 5 failures within 10 seconds). `Client` endpoints also support a retry mechanism that allows a client to resend failed requests periodically for a given number of times.

`Client` endpoints support Certificate Revocation List (CRL) and Online Certificate Status Protocol (OCSP). They also support HTTP2, keep-alive, chunking, HTTP caching, and data compression/decompression. 

See [Client Endpoint Example](https://ballerina.io/learn/by-example/http-client-endpoint.html), [Circuit Breaker Example](https://ballerina.io/learn/by-example/http-circuit-breaker.html), [HTTP Redirects Example](https://ballerina.io/learn/by-example/http-redirects.html)

### Listener endpoints

A `Service` represents a collection of network-accessible entry points and can be exposed via a `Listener` endpoint. A resource represents one such entry point and can have its own path, HTTP methods, body format, 'consumes' and 'produces' content types, CORS headers, etc. In resources, `endpoint` and `http:Request` are mandatory parameters while `path` and `body` are optional. 

When a `Service` receives a request, it is dispatched to the best-matched resource.


See [Listener Endpoint Example](https://ballerina.io/learn/by-example/http-data-binding.html), [HTTP CORS Example](https://ballerina.io/learn/by-example/http-cors.html), [HTTP Failover Example](https://ballerina.io/learn/by-example/http-failover.html), [HTTP Load Balancer Example](https://ballerina.io/learn/by-example/http-load-balancer.html)

`Listener` endpoints can be exposed via SSL. They support Mutual SSL, Hostname Verification, and Server Name Indication (SNI), and Application Layer Protocol Negotiation (ALPN). `Listener` endpoints also support OCSP Stapling, HTTP2, keep-alive, chunking, HTTP caching, and data compression/decompression. 

See [Mutual SSL Example](https://ballerina.io/learn/by-example/mutual-ssl.html).

See [Caching Example](https://ballerina.io/learn/by-example/caching.html), [HTTP Disable Chunking Example](https://ballerina.io/learn/by-example/http-disable-chunking.html).

### WebSockets

The package also provides support for WebSockets. There are two types of WebSocket endpoints: `WebSocketClient` and `WebSocketListener`. Both endpoints support all WebSocket frames. The `WebSocketClient` has a callback service.

There are also two types of services for WebSocket: `WebSocketService` and `WebSocketClientService`. The callback service for `WebSocketClient` is always a `WebSocketClientService`. The WebSocket services have a fixed set of resources that do not have a resource config. The incoming messages are passed to these resources.

**WebSocket upgrade**: During a WebSocket upgrade, the initial message is an HTTP request. To intercept this request and make the upgrade explicitly with custom headers, the user must create an HTTP resource with WebSocket specific configurations as follows:
```ballerina
 @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{name}",
            upgradeService: chatApp
        }
    }
    upgrader(endpoint caller, http:Request req, string name) {
    }
}
```
Here `upgradeService` is a `WebSocketService`.

**onOpen resource**: As soon as the WebSocket handshake is completed and the connection is established, the `onOpen` resource is dispatched. This resource is only available in the `WebSocketService`.

**onText resource**: Received Text messages are dispatched to this resource.

**onBinary resource**: Received Binary messages are dispatched to this resource.

**onPing and onPong resources**: Received ping and pong messages are dispatched to these resources respectively as a `blob`

**onIdleTimeout**: This resource is dispatched when idle timeout is reached. idleTimeout has to be configured by the user.

**onClose**: This resource is dispatched when a close message is recieved.



See [WebSocket Basic Example](https://ballerina.io/learn/by-example/websocket-basic-sample.html), 
[HTTP to WebSocket Upgrade Example](https://ballerina.io/learn/by-example/http-to-websocket-upgrade.html),
[WebSocket Chat Application](https://ballerina.io/learn/by-example/websocket-chat-application.html)

### Logging

This package supports two types of logs:
- HTTP access logs: These are standard HTTP access logs that are formatted using the combined log format and logged at the `INFO` level. Logs can be published to the console or a file using the following configurations:
    - `b7a.http.accesslog.console=true`
    - `b7a.http.accesslog.path=<path_to_log_file>`
- HTTP trace logs: These are detailed logs of requests coming to/going out of and responses coming to/going out of service endpoints or a client endpoints. Trace logs can be published to the console, to a file or to a network socket using the following set of configurations:
    - `b7a.http.tracelog.console=true`
    - `b7a.http.tracelog.path=<path_to_log_file>`
    - `b7a.http.tracelog.host=<host_name>`
    - `b7a.http.tracelog.port=<port>`
    
To publish logs to a socket, both the host and port configurations must be provided.  

See [HTTP Access Logs Example](https://ballerina.io/learn/by-example/http-access-logs.html), [HTTP Trace Logs Example](https://ballerina.io/learn/by-example/http-trace-logs.html)

## Samples

A `Client` endpoint can be defined using the URL of the remote service that the client needs to connect with, as shown below:

``` ballerina
endpoint http:Client clientEndpoint {
      url: "https://my-simple-backend.com"
   };
```
The defined `Client` endpoint can be used to call a remote service as follows:

``` ballerina
// Send a GET request to the specified endpoint
var response = clientEndpoint->get("/get?id=123");
```

A `Listener` endpoint can be defined as follows:

```ballerina
// Attributes associated with the `Listener` endpoint are defined here.
endpoint http:Listener helloWorldEP {
   port:9090
};
```

Then a `Service` can be defined and bound to the above `Listener` endpoint as shown below:

```ballerina
// By default, Ballerina assumes that the service is to be exposed via HTTP/1.1.
@http:ServiceConfig { basePath:"/helloWorld" }
service helloWorld bind helloWorldEP {

   // All resources are invoked with arguments of server connector and request.
   @http:ResourceConfig {
        methods:["POST"],
        path:"/{name}",
        body:"message"
    }
   sayHello (endpoint caller, http:Request req, string name, string message) {
       http:Response res = new;
       // A util method that can be used to set string payload.
       res.setPayload("Hello, World! I’m " + name + “. “ + message);
       // Sends the response back to the client.
       caller->respond(res) but { error e => 
                            log:printError("Error sending response", err = e) };
   }
}
```
