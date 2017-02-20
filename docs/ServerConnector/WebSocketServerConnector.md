#How to write a WebSocket Server Connector

##WebSocket?
WebSocket is a protocol which provides full-duplex, persistent communication channels over a single TCP connection.
Once a WebSocket connection is established the connection stays open until the client or server decides to 
close this connection. This connection is also initialized by the client. 
For that WebSocket protocol uses HTTP request to upgrade the connection from HTTP to WebSocket using two 
HTTP headers, “Connection” and “Upgrade”.

<br>

_eg: <br>
GET ws://websocket.example.com/ HTTP/1.1<br>
Origin: http://example.com<br>
Connection: Upgrade<br>
Host: websocket.example.com<br>
Upgrade: websocket<br>_

This indicate server that the client needs to establish a websocket connection. If the server supports websocket then it send the response agreeing to establish connection using the “Upgrade” header.
<br>

_eg: <br>
HTTP/1.1 101 WebSocket Protocol Handshake <br>
Date: Wed, 16 Oct 2013 10:07:34 GMT <br>
Connection: Upgrade <br>
Upgrade: WebSocket <br>_

Once this message is sent the handshake is completed and initial HTTP 
connection is replaced by websocket connection using 
the same underlying TCP/IP connection.

So Ballerina uses the same HTTP base path + WebSocket 
Upgrade path to upgrade the existing connection to a WebSocket 
connection. 

##How to define a WebSocket Service in Ballerina
WebSocket Service is defined in the same service 
base path of a rest service since this is a upgrade 
from the rest service.

###Step 1 - Adding WebSocketUpgradePath
Add a base path and WebSocketUpgradePath to the service.

Eg :
```ballerina
@http:BasePath(“/test”)
@ws:WebSocketUpgradePath(“/websocket”)
service myService {
}
```
If you want to connect to this WebSocket endpoint use 
_ws://host:port/test/websocket_

###Step 2 - Adding annotations to the resource level
There are several supported annotations for this release of Ballerina. 

####@ws:OnOpen
This annotated resource will be called when  a new 
client is connecting to the WebSocket endpoint. 
So if something has to be done when a new client is 
connecting to the endpoint you can add that logic to 
the relevant resource.

####@ws:OnTextMessage
When the server is receiving a text message from 
client this resource will be called

####@ws:OnClose
When client connection is closed due to some reason this resource will be called. 

####Example
```ballerina
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

###Functions for WebSocket
####ws:sendText(message, string)
This function is used if the user need 
to send a text message to the same client who sent 
the WebSocket message.

|Parameter Type|Description|Expected Values|
|--------------|-----------|---------------|
|message|The message which received as the resource parameter|This shouldn’t be null. Message contains all the details which are needed to find out where to send and who should receive text|
|string|The text message which is need to be sent|This can be any string. Which is need to bes sent.|


Please refer the simple echo server example given in
samples/websocket/echo-server/server/websocketEchoServer.bal
