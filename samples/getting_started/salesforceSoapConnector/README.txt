Description
===========
This is a sample Salesforce connector with addAccount action. And the action is being invoked in a ballerina main function.

Prerequisites
=============
1. Create a Salesforce account.
2. Obtain the following parameters
    * Username
    * Password
    * SecurityToken

How to run this sample
======================
bin$ ./ballerina ../samples/salesforceSoapConnector/salesforceSoapConnector.bal <username> <password+securityToken> <message1> <message2>