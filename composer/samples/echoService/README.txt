Description
===========
This sample will echo the request message


How to run this sample
======================
bin$ ./ballerina run ../samples/echoService/echoService.bal

the above command will start the 'echoService" service in echoService.bal.


Invoking the service
====================
curl -v http://localhost:9090/echo -d "Hello World"
