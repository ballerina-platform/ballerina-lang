Description
===========
This sample will reply with a greeting message.


How to run this sample
======================
bin$ ./ballerinaserver.sh ../samples/helloWorldService/helloWorldService.bal

the above command will start the ballerina server in the current terminal and deploy the helloWorldService.bal file and publish the service 'helloWorldService'.


Invoking the service
====================
curl -v http://localhost:9090/hello?name=wso2

Here the query parameter 'name' is optional, if we don't specify the name the greeting will not have any name.