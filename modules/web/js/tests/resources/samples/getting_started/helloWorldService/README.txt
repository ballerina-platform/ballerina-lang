Description
===========
This sample runs a service that replies to a request with a greeting message.


How to run this sample
======================
bin$ ./ballerina run service ../samples/helloWorldService/helloWorldService.bal

The above command will start the ballerina server in the current terminal, deploy the helloWorldService.bal file, and publish the service 'helloWorldService'.


Invoking the service
====================
curl -v http://localhost:9090/hello


What did it do?
===============
When you ran the sample, you started the Ballerina server and deployed the helloWorld service.
You sent a curl request that specified the Ballerina server host and port followed by the base path ("/hello") for the helloWorld service, thereby invoking that service.
The helloWold service received the curl request, created a response message, and sent it back to the client that invoked the service: your terminal.
