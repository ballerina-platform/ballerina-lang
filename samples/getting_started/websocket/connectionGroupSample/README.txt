Description
===========
This sample provide the way of creating WebSocket connection groups globally and invoking them in other services.
In this sample there are two services.

oddEvenWebSocketService
-----------------------
All the WebSocket connections will be created here. But there is a internal count which identifies whether the
connection count is odd or even. Depending on the count the WebSocket clients will be separated into two groups,
"oddGroup" and "evenGroup". In the same service when a new message is coming from a client it will be sent to those two
groups as separate messages.

oddEvenHttpService
------------------
This is a http service which has several endpoints
* /groupInfo/even
 The message will be delivered to the evenGroup which is created in oddEvenWebSocketService.
* /groupInfo/odd
 The message will be delivered to the oddGroup which is created in oddEvenWebSocketService.
* /groupInfo/rm-even
 This will delete the evenGroup which is created in oddEvenWebSocketService.
* /groupInfo/rm-odd
 This will delete the oddGroup which is created in oddEvenWebSocketService.
* /groupInfo/close-even
 This will close the evenGroup which is created in oddEvenWebSocketService.
* /groupInfo/close-odd
 This will close the oddGroup which is created in oddEvenWebSocketService.

Pre-requisites
==============
Ability to run few WebSocket clients (Doesn't matter the client. Just need a client to connect to the endpoint.)
ex: WebSocket Browser client

How to run the sample
=====================
bin$ ./ballerina run ../samples/websocket/connectionGroupSample/oddEvenWebSocketSample.balx

In order to work both those services should run parallel.

Checking the capability
=======================
First connect to the WebSocket endpoint using several WebSocket clients. When they are connecting remember the order that they are connected.
Then post some data to any http endpoint that you like mentioned above and see how those messages are delivered to the accurate group of connections.

Once you read the sample .bal files you can learn more about how to create WebSocket connection groups.
