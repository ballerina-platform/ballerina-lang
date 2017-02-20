# Testerina

Testerina is the test framework built for the Ballerina language.  
This will be a part of ```ballerina-tools-0.8.0.zip``` distribution [1].

Testerina provides the ```ballerina test``` command.  

To test a file written in Ballerina language, use the test command as follows.  
```./ballerina test <package_name>```

Your test file should contain ```_test.bal``` suffix.

Once you run the tests using ```ballerina test``` command,  
Testerina will print a summary of test results on the console.

#### Testerina provides following functions.

package ballerina.test;

 - startService(string servicename)
 - assertTrue(boolean condition)
 - assertTrue(boolean condition, string message)
 - assertFalse(boolean condition)
 - assertFalse(boolean condition, string message) 
 - assertEquals(string actual, string expected)
 - assertEquals(string actual, string expected, string message)
 - assertEquals(int actual, int expected)
 - assertEquals(int actual, int expected, string message)
 - assertEquals(float actual, float expected)
 - assertEquals(float actual, float expected, string message)
 - assertEquals(boolean actual, boolean expected)
 - assertEquals(boolean actual, boolean expected, string message)
 - assertEquals(string[] actual, string[] expected)
 - assertEquals(string[] actual, string[] expected, string message)
 - assertEquals(float[] actual, float[] expected)
 - assertEquals(float[] actual, float[] expected, string message)
 - assertEquals(int[] actual, int[] expected)
 - assertEquals(int[] actual, int[] expected, string message)
 
package mock;
 - setValue(string pathExpressionToMockableConnector)
 
 
### Writing ballerina tests

- Test functions should contain the prefix ```test```.  
e.g.: ```testAddTwoNumbers()```
- Each test function may contain one or more asserts.  
e.g. 1: 
```
import ballerina.test;
function testAddTwoNumbers() {
    test:assertEquals(addTwoNumbers(1, 2), 3, "Number addition failed for positive numbers");
    test:assertEquals(addTwoNumbers(-1, -2), -3, "Number addition failed for negative numbers");
    test:assertEquals(addTwoNumbers(0, 0), 0, "Number addition failed for number zero");
}
```

e.g. 2:
This example shows how to test ballerina service with the back-end mocking support.
```
import ballerina.mock;
import ballerina.test;
import ballerina.lang.messages;

function testService() {
    string serviceURL = test:startService("helloWorld");
    string mockedTwitterServiceURL = test:startService("mockedTwitterService");
    mock:setValue("helloWorld.myTwitterConnectorInstance.myHttpConnector.serviceUri", mockedTwitterServiceURL);
    
    http:ClientConnector client = create http:ClientConnector(serviceURL);
    message request = {};
    message response = {};
    response = http:ClientConnector.get(client, "/", request);
    string payload = messages:getStringPayload(response);
    test:assertEquals(payload, "<expectedOutput/>");
}
```
> A complete sample can be found at `samples/mock/` directory.

If at least one assert fails, whole test function will be marked as failed.
Detailed information is shown in the test result summary.  
> One package may contain more than one ```*._test.bal``` file.

#### Tutorial

1 Download ```ballerina-tools-0.8.0.zip``` distribution and unzip.  
2 Unzip and go to ```ballerina-tools-0.8.0```.  
3 Create a directory ```mySample```.  
4 Create the following two files inside this directory.  

e.g.: sample.bal
```
package mySample;

import ballerina.lang.system;
 
function main (string[] args) {
    int i = intAdd(1, 2);
    system:println("Result: " + i);
}
 
function intAdd(int a, int b) (int) {
    return a + b;
}

```  
e.g.: sample_test.bal
```
package mySample;
 
import ballerina.test;
 
function testInt() {	
    int answer = 0;
    answer = intAdd(1, 2);
    test:assertEquals(answer, 3, "IntAdd function failed");
	
}
```
Note the package hierarchy in above files.   
 
5 Run tests using following command.  
```> ./bin/ballerina test mySample/```  

Following is a sample console output. 

```
error in 'testInt': <Detail error message>
 
Results: 
 Tests Run: 1, Passed: 0, Failed: 1
 
Failed Tests:
 testInt: <Detail error message>
```

### Running Samples

#### Running the mock sample
- Download ```ballerina-tools-0.8.0.zip``` distribution and unzip.  
- Copy ```samples/mock``` directory to ```ballerina-tools-0.8.0```.  
- Run tests as follows.  
```> ./bin/ballerina test mock/```

### Reference:  
[1] https://github.com/ballerinalang/distribution