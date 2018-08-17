# Test Connector

Allows connecting Test REST API.

Aliquam ut massa in turpis dapibus convallis. Praesent elit lacus, vestibulum at malesuada et, ornare et est. Ut augue nunc, 
sodales ut euismod non, adipiscing vitae orci. Mauris ut placerat justo. Mauris in ultricies enim. Quisque nec est eleifend 
nulla ultrices egestas quis ut quam. Donec sollicitudin lectus a mauris pulvinar id aliquam urna cursus. Cras quis ligula sem, 
vel elementum mi. Phasellus non ullamcorper urna.


## Compatibility

| Ballerina Language Version  | Test Basic API Version | Test Authy API Version |
|:---------------------------:|:------------------------:|:------------------------:|
| 0.970.0-beta18              | 2010-04-01               | v1                       |

## Getting started

1.  Refer the [Getting Started](https://ballerina.io/learn/getting-started/) guide to download and install Ballerina.

2. Create a new Ballerina project by executing the following command.

	```shell
	   <PROJECT_ROOT_DIRECTORY>$ ballerina init
	```

4. Import the Test package to your Ballerina program as follows.

	```ballerina
	   import IntegrationTest/test;
       import ballerina/io;

	   function main (string... args) {
         int result = test:add(5, 6);
         io:println(result);
	   }
	```
