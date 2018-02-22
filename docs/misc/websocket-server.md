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

## Functions of WebSocket

### pushing messages to the same client
#### ws:pusText(text)
This function is used if the user needs to send a text message to the same client who sent the WebSocket message.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|text|string|The text message to send|This can be any string|

For an example, see the simple echo server sample in `samples/websocket/echo-server/server/websocketEchoServer.bal`.

### Pushing messages to all the clients connected to a given service
#### ws:broadcastText(text)
This function push text all the clients who have been connected to the currently this function declared service.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|text|string|The text message to send|This can be any string|

### Closing the connection of the current client from server side
#### ws:closeConnection(text)
This function close the connection of the current client from a given WebSocket resource.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|none|-|-|-|

### Storing and grouping WebSocket connections globally
In Ballerina you can store the WebSocket connections and use them in other services too.
There are 2 ways of using this feature.
- Storing single connection
- Grouping connections

### Storing single connection
#### ws:storeConnection(connectionName)
This function is used to store a given connection globally and it can be
retrieved by any other service in Ballerina to push data from server to 
client.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|connectionName|string|Represent the name of the connection given by the user|A unique string|

_note: connection name should be a unique string for a connection. 
If the same connection name used again, the previous connection will be 
replaced by the current connection._

#### ws:pushTextToConnection(connectionName, text)
This function can be used in any service to retrieve a connection which 
was previously stored by the user and push text to the client.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|connectionName|string|Represent the name of the connection given by the user|A unique string|
|text|string|The text message to send|This can be any string|

#### ws:removeStoredConnection(connectionName);
This function is used to remove the connection from the global connection 
store.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|connectionName|string|Represent the name of the connection given by the user|Unique string of the connection|

#### ws:closeStoredConnection(connectionName);
This function is used to close a stored connection. Note that removeStoredConnection will only remove connection from
connection store. This will close the connection and remove it from the store.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|connectionName|string|Represent the name of the connection given by the user|Unique string of the connection|

### Grouping WebSocket Connections
#### ws:addConnectionToGroup(groupName)
This will add the current WebSocket connection to group with the given group name.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|groupName|string|Represent the name of the group which current connection is added|String name represents the group|

#### ws:pushTextToGroup(groupName, text)
This function can be used globally to access the connection groups defined by any of the services and send text to that
specific group.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|groupName|string|Represent the name of the group|String name represents the group|
|text|string|The text message to send|This can be any string|

#### ws:removeConnectionFromGroup(groupName)
This function removes the current connection from the given group if that connection exists in the given group.

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|groupName|string|Represent the name of the group|String name represents the group|

#### ws:removeConnectionGroup(groupName)
This removes the whole connection group globally. So if the connection group is removed it cannot be used again.
But it does not mean that individual connections are removed globally. Because same connection can be added to one or
more groups. 

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|groupName|string|Represent the name of the group|String name represents the group|

#### ws:closeConnectionGroup(groupName)
This close all the connections in a connection group. Note that removeConnectionGroup will only remove only the connection
group. But the connection status will not affect. But here the connections will be closed and removed the connection group

|Parameter|Parameter Type|Description|Expected Values|
|---------|--------------|-----------|---------------|
|groupName|string|Represent the name of the group|String name represents the group|

