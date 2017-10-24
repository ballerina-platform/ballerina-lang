Description
===========
The 'tcpServer.bal' will start a tcp server and listen on port 8989

the 'tcpClient.bal' will read a sample csv file and stream it to the tcp server


Prerequisites
=============

The "sample.csv" file is located in the directory $Ballerina_HOME/samples/io/files/ hence ensure that "tcpClient
.bal" is executed from $Ballerina_HOME/bin directory

How to run this sample
======================

1. Run the server

bin$ ./ballerina run ../samples/io/tcpServer.bal


2. Run the client

bin$ ./ballerina run ../samples/io/tcpClient.bal
