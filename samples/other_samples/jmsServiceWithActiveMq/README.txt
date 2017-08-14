Description
===========
This sample contains two services which listens to a queue and topic respectively. Whenever the message is delivered
to the queue, the same message is published to the message

Pre-requisites
--------------
1. Start the activemq broker.
2. Copy the client libs of activemq broker to {Ballerina_HOME}/bre/lib folder.
3. Copy the jmsServiceWithActiveMq.bsz in this folder to {Ballerina_HOME}/samples/ folder

How to run this sample
======================
bin$ ./ballerina run ../samples/jmsServiceWithActiveMq.bsz

the above command will start the ballerina server in the current terminal and deploy the jmsServiceWithActiveMq.bsz
and publish the 'jmsActiveMqQueueService' and 'jmsActiveMqTopicService' services

Invoking the service
====================
Publish map messages to the queue "ballerinaqueue" with the map key "queue message count" and you will be able to see
the relevant messages passed to the topic "ballerinatopic" and they are getting printed in the console.
