Description
===========
The 'jmsReceiver.bal' will start a jms listener to queue 'MyQueue'.

the 'jmsSender.bal' will send a jms message to 'MyQueue' so that the above receive can receive it.


How to run this sample
======================

Copy activemq libraries that are required for client connection to <ballerina_home>/bre/lib folder.

These would either be

    activemq-all-<version>.jar

or the following set of jars.

    1. activemq-core-<version>.jar
    2. geronimo-j2ee-management_1.1_spec-<version>.jar
    3. geronimo-j2ee-management_1.1_spec-<version>.jar

After copying the agove client libraries we can run the samples.

1. Run the receiver

bin$ ./ballerina run service ../samples/jms/jmsReceiver.bal


2. Run the sender

bin$ ./ballerina run main ../samples/jms/jmsSender.bal
