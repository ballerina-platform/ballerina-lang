Description
===========
This sample contains two services which listens to a queue and topic respectively. Whenever the message is delivered
to the queue, the same message is published to the message

Pre-requisites
--------------
1. Start the WSO2 Message Broker.
2. Copy the client libs of WSO2 Message broker to {Ballerina_HOME}/bre/lib folder.
3. Copy the jndi.properties in this folder to {Ballerina_HOME}/bin folder

How to run this sample
======================
bin$ ./ballerina service ../samples/jmsServiceWithMB/jmsServiceWithMB.bal

the above command will start the ballerina server in the current terminal and deploy the jmsServiceWithMB.bal file
and publish the 'jmsWSO2MBQueueService' and 'jmsWSO2MBTopicService' services

Invoking the service
====================
Publish map messages to the queue with the name "ballerinaqueue" with the map key "queue message count" and you will be
able to see the relevant messages passed to the topic "ballerinatopic" and they are getting printed in the console.