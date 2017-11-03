Description
===========
This sample runs a service that replies to a request with a greeting message.


How to run this sample
======================
bin$ ./ballerina run ../samples/helloWorldService/helloWorldService.bal

The above command will run the ballerina program and start the service 'helloWorldService'.


Invoking the service
====================
curl -v http://localhost:9090/hello


What did it do?
===============
First you started the 'helloWorld' service. Then you sent a curl request to invoke that service.
The helloWold service received the curl request, created a response message, and sent it back to the client that
invoked the service: your terminal.
