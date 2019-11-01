## Module overview

This module provides an implementation for connecting and interacting with HTTP, HTTP2, and WebSocket endpoints. The module facilitates two types of endpoints as ‘Client’ and ‘Listener’.

### Client endpoints

`Client` endpoints are used to connect to and interact with HTTP endpoints. They support connection pooling and can be configured to have a maximum number of active connections that can be made with the remote endpoint. `Client` endpoints activate connection eviction after a given idle period and also support follow-redirects so that the users do not have to manually handle 3xx HTTP status codes.

`Client` endpoints handle resilience in multiple ways such as load balancing, circuit breaking, endpoint timeouts, and a retry mechanism.

Load balancing is used in the round robin or failover manner.

When a failure occurs in the remote service, the client connections might wait for some time before a timeout occurs. Awaiting requests consume resources in the system. Circuit Breakers are used to trip after a certain number of failed requests to the remote service. Once a circuit breaker trips, it does not allow the client to send requests to the remote service for a period of time.

The Ballerina circuit breaker supports tripping on HTTP error status codes and I/O errors. Failure thresholds can be configured based on a sliding window (e.g., 5 failures within 10 seconds). `Client` endpoints also support a retry mechanism that allows a client to resend failed requests periodically for a given number of times.

`Client` endpoints support Server Name Indication (SNI), Certificate Revocation List (CRL), Online Certificate Status Protocol (OCSP), and OCSP Stapling for SSL/TLS connections. They also support HTTP2, keep-alive, chunking, HTTP caching, data compression/decompression, and authentication/authorization.

A `Client` endpoint can be defined using the URL of the remote service that the client needs to connect with, as shown below:

``` ballerina
http:Client clientEndpoint = new("https://my-simple-backend.com");
```
The defined `Client` endpoint can be used to call a remote service as follows:

``` ballerina
// Send a GET request to the specified endpoint.
var response = clientEndpoint->get("/get?id=123");
```

For more information, see [Client Endpoint Example](https://ballerina.io/learn/by-example/http-client-endpoint.html), [Circuit Breaker Example](https://ballerina.io/learn/by-example/http-circuit-breaker.html), [HTTP Redirects Example](https://ballerina.io/learn/by-example/http-redirects.html).

### Listener endpoints

A `Service` represents a collection of network-accessible entry points and can be exposed via a `Listener` endpoint. A resource represents one such entry point and can have its own path, HTTP methods, body format, 'consumes' and 'produces' content types, CORS headers, etc. In resources, `http:caller` and `http:Request` are mandatory parameters while `path` and `body` are optional.

When a `Service` receives a request, it is dispatched to the best-matched resource.

A `Listener` endpoint can be defined as follows:

```ballerina
// Attributes associated with the `Listener` endpoint are defined here.
listener http:Listener helloWorldEP = new(9090);
```

Then a `Service` can be defined and attached to the above `Listener` endpoint as shown below:

```ballerina
// By default, Ballerina assumes that the service is to be exposed via HTTP/1.1.
@http:ServiceConfig { basePath: "/helloWorld" }
service helloWorld on helloWorldEP {

   // All resource functions are invoked with arguments of server connector and request.
   @http:ResourceConfig {
       methods: ["POST"],
       path: "/{name}",
       body: "message"
   }
   resource function sayHello(http:Caller caller, http:Request req, string name, string message) {
       http:Response res = new;
       // A util method that can be used to set string payload.
       res.setPayload("Hello, World! I’m " + <@untainted> name + ". " + <@untainted> message);
       // Sends the response back to the client.
       var result = caller->respond(res);
       if (result is http:ListenerError) {
            error err = result;
            log:printError("Error sending response", err = err);
       }
   }
}
```

See [Listener Endpoint Example](https://ballerina.io/learn/by-example/http-data-binding.html), [HTTP CORS Example](https://ballerina.io/learn/by-example/http-cors.html), [HTTP Failover Example](https://ballerina.io/learn/by-example/http-failover.html), [HTTP Load Balancer Example](https://ballerina.io/learn/by-example/http-load-balancer.html)

`Listener` endpoints can be exposed via SSL. They support Mutual SSL, Hostname Verification, and Application Layer Protocol Negotiation (ALPN) for HTTP2. `Listener` endpoints also support Certificate Revocation List (CRL), Online Certificate Status Protocol (OCSP), OCSP Stapling, HTTP2, keep-alive, chunking, HTTP caching, data compression/decompression, and authentication/authorization.

For more information, see [Mutual SSL Example](https://ballerina.io/learn/by-example/mutual-ssl.html).

For more information, see [Caching Example](https://ballerina.io/learn/by-example/cache.html), [HTTP Disable Chunking Example](https://ballerina.io/learn/by-example/http-disable-chunking.html).

### WebSocket

This module also provides support for WebSockets. There are two types of WebSocket endpoints: `WebSocketClient` and `WebSocketListener`. Both endpoints support all WebSocket frames. The `WebSocketClient` has a callback service.

There are two types of services for WebSockets. The service of the server has the `WebSockerCaller` as the resource parameter and the callback service of the client has `WebSocketClient` as the resource parameter. The WebSocket services have a fixed set of resources that do not have a resource config. The incoming messages are passed to these resources.

**WebSocket upgrade**: During a WebSocket upgrade, the initial message received is an HTTP request. To intercept this request and perform the upgrade explicitly with custom headers, the user must create an HTTP resource with WebSocket-specific configurations as follows:

```ballerina
@http:ResourceConfig {
    webSocketUpgrade: {
        upgradePath: "/{name}",
        upgradeService: chatApp
    }
}
resource function upgrader(http:Caller caller, http:Request req, string name) {
}
```
The `upgradeService` is a server callback service.

**onOpen resource**: As soon as the WebSocket handshake is completed and the connection is established, the `onOpen` resource is dispatched. This resource is only available in the service of the server.

**onText resource**: The received text messages are dispatched to this resource.

**onBinary resource**: The received binary messages are dispatched to this resource.

**onPing and onPong resources**: The received ping and pong messages are dispatched to these resources respectively.

**onIdleTimeout**: This resource is dispatched when the idle timeout is reached. The `idleTimeout` has to be configured either in the WebSocket service or the client configuration.

**onClose**: This resource is dispatched when a close frame with a statusCode and a reason is received.

**onError**: This resource is dispatched when an error occurs in the WebSocket connection. This will always be preceded by a connection closure with an appropriate close frame.

For more information, see [WebSocket Basic Example](https://ballerina.io/learn/by-example/websocket-basic-sample.html), [HTTP to WebSocket Upgrade Example](https://ballerina.io/learn/by-example/http-to-websocket-upgrade.html), [WebSocket Chat Application](https://ballerina.io/learn/by-example/websocket-chat-application.html), [WebSocket Proxy Server](https://ballerina.io/learn/by-example/websocket-proxy-server.html).

### Logging

This module supports two types of logs:
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
