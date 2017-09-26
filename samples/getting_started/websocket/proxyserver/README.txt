Description
===========
This is a WebSocket proxy written in ballerina which will pass messages from client to a remote server and when a message is received to the client service it will send it back to the client which connected to the proxy server.

How to run the sample
=====================
bin$ ./ballerina run ../samples/websocket/proxyserver/proxyserver.balx


How to connect
==============
Connect to ws://localhost:9090/proxy/ws using any WebSocket client and send messages to check