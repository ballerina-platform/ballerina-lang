Description
===========
This sample will echo the request message


How to run this sample
======================
bin$ ./ballerina run service ../samples/echoService/echoService.bal

the above command will start the ballerina server in the current terminal and deploy the echoService.bal file and publish the service 'echoService'.


Invoking the service
====================
curl -v http://localhost:9090/echo -d "Hello World"
