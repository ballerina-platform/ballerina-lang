This sample will reply echo the request message


How to run this sample
======================

bin$ ./ballerinaserver.sh ../samples/Sample5/EchoService.bal

the above command will start the ballerina server in the current terminal and deploy the EchoService.bal file and publish the service 'EchoService'.


Invoking the service
====================

curl -v http://locahost:9090/echo -d "Hello Wold"