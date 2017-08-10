Description
===========
The 'jmsReceiver.bal' will start a jms listener to queue 'MyQueue'.

the 'jmsSender.bal' will send a jms message to 'MyQueue' so that the above receive can receive it.


Prerequisites
=============

Before running this sample,

    1. Start an instance of Apache ActiveMQ
    2. Create a message queue named 'MyQueue'

For information on getting started with ActiveMQ: http://activemq.apache.org/getting-started.html


How to run this sample
======================

Copy activemq libraries that are required for client connection to <ballerina_home>/bre/lib folder.

These would either be

    activemq-all-<version>.jar

or the following set of jars.

    1. activemq-core-<version>.jar
    2. geronimo-j2ee-management_1.1_spec-<version>.jar
    3. geronimo-j2ee-management_1.1_spec-<version>.jar

After copying the above client libraries we can run the samples.

1. Run the receiver

bin$ ./ballerina run ../samples/jms/jmsReceiver.bal


2. Run the sender

bin$ ./ballerina run ../samples/jms/jmsSender.bal
