This sample will reply a greeting message.


How to run this sample
======================

bin$ ./ballerinaserver.sh ../samples/Sample4/HelloWorldService.bal

the above command will start the ballerina server in the current terminal and deploy the HelloWorldService.bal file and publish the service 'HelloWorldService'.


Invoking the service
====================

curl -v http://locahost:9090/hello?name=wso2

Here the query parameter 'name' is optional, if we don't specify the name the greeting will not have the name.