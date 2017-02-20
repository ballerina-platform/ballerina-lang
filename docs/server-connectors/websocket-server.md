# WebSocket Server Connector

WebSocket is a protocol that provides full-duplex, persistent communication channels over a single TCP connection. Once a WebSocket connection is established, the connection stays open until the client or server decides to close this connection. This connection is also initialized by the client, in which case the WebSocket protocol uses an HTTP request to upgrade the connection from HTTP to WebSocket using two HTTP headers, “Connection” and “Upgrade”.

For example:

```
GET ws://websocket.example.com/ HTTP/1.1
Origin: http://example.com
Connection: Upgrade
Host: websocket.example.com
Upgrade: websocket
```

This tells the server that the client needs to establish a WebSocket connection. If the server supports WebSocket, it sends the response agreeing to establish the connection using the “Upgrade” header.

For example:

```
HTTP/1.1 101 WebSocket Protocol Handshake
Date: Wed, 16 Oct 2013 10:07:34 GMT
Connection: Upgrade
Upgrade: WebSocket
```

Once this message is sent, the handshake is completed, and the initial HTTP connection is replaced by the WebSocket connection using the same underlying TCP/IP connection. Ballerina uses the same HTTP base path + WebSocket Upgrade path to upgrade the existing connection to a WebSocket 
connection. 

## Defining a WebSocket service in Ballerina
A WebSocket service is defined in the same service base path as a REST service, since this is an upgrade from the REST service.

### Step 1: Add WebSocketUpgradePath
Add a base path and WebSocketUpgradePath to the service.

For example:

```
@http:BasePath(“/test”)
@ws:WebSocketUpgradePath(“/websocket”)
service myService {
}
```
If you want to connect to this WebSocket endpoint, use `ws://host:port/test/websocket`. 

### Step 2: Add annotations to the resource level
There are several supported annotations for this release of Ballerina. 

#### @ws:OnOpen
This annotated resource will be called when a new client is connecting to the WebSocket endpoint. If something has to be done when a new client is connecting to the endpoint, you can add that logic to the relevant resource.

#### @ws:OnTextMessage
When the server is receiving a text message from the client, this resource will be called.

#### @ws:OnClose
When the client connection is closed, this resource will be called. 

#### Example

```
import ballerina.lang.messages;

@http:BasePath ("/test")
@ws:WebSocketUpgradePath("/websocket")
service helloWorld {
    @ws:OnOpen
    resource onOpenMessage(message m) {}

    @ws:OnTextMessage
    resource onTextMessage(message m) {}

    @ws:OnClose
    resource onCloseMessage(message m) {}
}
```

### Functions for WebSocket

#### ws:sendText(message, string)
This function is used if the user needs to send a text message to the same client who sent the WebSocket message.

|Parameter Type|Description|Expected Values|
|--------------|-----------|---------------|
|message|The message received as the resource parameter|This should not be null. The message should contain all the details needed for sending the text, such as the recipient|
|string|The text message to send|This can be any string|

For an example, see the simple echo server sample in `samples/websocket/echo-server/server/websocketEchoServer.bal`.
