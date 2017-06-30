Description
===========
This sample demonstrates file processing capability of Ballerina.

The sample will start a service called 'OrderProcessService' which will keep processing orders
where each order is an actual file, placed in a specific folder.

Scenario is as follows:
- Assume that there exists a folder in the local file system called "orders"
- Once an order is placed, a file is created in "orders" folder which contains order information.
- OrderProcessService continuously listens to this "orders" folder and once a file is found, it print the contents of the file on the console.


How to run this sample
======================
1. Copy the "orders" folder in resources folder of this sample to some location in your file system.

For instance, if you copied the orders folder to "/home/user", then the URI to the same folder will be "file:///home/user/orders"

2. Now edit the 'fileURI' parameter in orderProcessService.bal file, in such a way that it will contain the above URI.

For example:
fileURI = "file:///home/user/orders"

3. Now start the OrderProcessService by executing following command on the console:

bin$ ./ballerina run ../samples/fileService/orderProcessService.bal

4. Now you should see the order information of each order are getting printed on the console as below:

Info--order1
Info--order2
Info--order3
Info--order4
Info--order5
