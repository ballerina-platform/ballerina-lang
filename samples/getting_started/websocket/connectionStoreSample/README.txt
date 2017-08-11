Description
===========
This sample demonstrate how to store connections individually and how to use those connection globally among services.
This sample has two services

websocketEndpoint
-----------------
This is where the WebSocket clients are connecting. When they are connecting each and every connection will be given
a unique id and will be store as unique connections globally.

httpService
-----------
Http service is where some http client can POST some data to the endpoint with path parameter which is a id for a given
connection or can remove them using a GET.

How to run this sample
======================
bin$ ./ballerina run ../samples/websocket/connectionStoreSample/storeConnection.balx

Invoking the service
====================
To invoke the service first run few WebSocket clients and connect to webSocketEndpoint. Then send some http POST requests with a unique id to send it back to the same client which has that id.