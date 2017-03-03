Description
===========
This sample integrates File Server Connector with JMS Client Connector.

In this sample, the content of each file in a certain folder will be published as a message, to a queue in activemq broker.

The sample will start a service called 'testFileService' which will keep processing files in the "orders" folder.
After processing each file, a message will be published to a queue called "order".

How to run this sample
======================
1. Copy the "orders" folder in resources folder of this sample to some location in your file system.

For instance, if you copied the orders folder to "/home/user", then the URI to the same folder will be "file:///home/user/orders"

2. Now edit the 'fileURI' parameter in testFileService.bal file, in such a way that it will contain the above URI.

For example:
fileURI = "file:///home/user/orders"

3. Start the activemq broker.

4. Copy the client libs of activemq broker to {Ballerina_HOME}/bre/lib folder.

5. Now start the testFileService by executing following command on the console:

bin$ ./ballerina run ../samples/fileServiceWithActiveMq/testFileService.bal

4. After a few seconds, you should see the order information of each order are getting printed on the console as below:

Info--order1
Info--order2
Info--order3
Info--order4
Info--order5

5. In addition to that, browse the queue "order" on the activemq broker. You should be able to see the same messages in the queue.
