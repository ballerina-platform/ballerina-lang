## Module overview

This module facilitates developers to write automation tests for ballerina code in a simple manner. It provides a number of capabilities such as configuring setup and cleanup steps in different levels, ordering and grouping of tests, providing value-sets to tests and independence from external functions and endpoints via mocking capabilities.

## Annotations
A ballerina testsuite can be implemented using a set of annotations. The available annotations enable executing instructions before and after the testsuite or a single test, organize a set of tests into a group, define data-driven tests, specify an order of execution, disable tests and mocking.

The following example shows a simple testsuite.
```ballerina
import ballerina/io;
import ballerina/test;

# Before Suite Function
@test:BeforeSuite
function beforeSuiteFunc() {
    io:println("I'm the before suite function!");
}

# Before test function
function beforeFunc() {
    io:println("I'm the before function!");
}

# Test function
@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}

# After test function
function afterFunc() {
    io:println("I'm the after function!");
}

# After Suite Function
@test:AfterSuite
function afterSuiteFunc() {
    io:println("I'm the after suite function!");
}
```
The following example shows how an individual test can be configured.
```ballerina
@test:Config{  
    enable: false, // default is true
    before: "init",
    after: "cleanup",
    dependsOn: ["test1"],
    dataProvider:"dataGen"
}
function dataProviderTest (int value) returns error? {
    test:assertEquals(value, 1, msg = "value is not correct");
}

function dataGen() returns (int[][]) {
    return [[1]];
}
```

## Assertions
This module provides a number of assertions in order to verify the expected behaviour of a piece of code. These assertions can be used to decide if the test is passing or failing based on the condition.

Following sample shows how to use assertions in Testerina.
```ballerina

import ballerina/test;

@test:Config{}
function testAssertIntEquals() {
    int answer = 0;
    int a = 5;
    int b = 3;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, msg = "int values not equal");
}

function intAdd(int a, int b) returns (int) {
    return (a + b);
}
```

## Mocking

The test module provides capabilities to mock a function or an object for unit testing. The mocking features can be used to control the behavior of functions and objects by defining return values or replacing the entire object or function with a user-defined equivalent. This feature will help you to test the Ballerina code independently from other modules and external endpoints.

### Function Mocking
Function mocking allows to control the behavior of a function in the module under test or a function of an imported module. 

An annotation is used to declare a mock function within which the original function to be mocked should be defined using the annotation parameters. The function specified with this annotation allows to mock an imported function whereas the same annotation specified with the test:MockFunction object allows to mock a function in the module under test.

#### Mocking an imported function

Mocking an imported function will invoke the mock function every time that the original function is called. Mocking a function will apply the mocked function to every instance of the original function call. It is not limited to the test file where it is being mocked. A compile time error will be thrown in case of a signature mismatch between the mock function and the original.

```ballerina
The following example shows how an imported function can be mocked.

@test:Mock {
    moduleName : "ballerina/math",
    functionName : "sqrt"
}
function mocksqrt(float a) returns (float) {
    return a*a*a;
}

@test:Config {}
function testMockImportedFunction() {
    float answer = 0;
    answer = math:sqrt(5);
    test:assertEquals(answer, 125.0);
}
```

#### Mocking a function in the same module

The same annotation used for mocking an imported function can be used for mocking a function in the same module (the module which is under test) but with the MockFunction object provided by the test module. Declaring the annotation with this object will create a default mock object in place of the original function. 

Subsequent to the declaration, the function call should be stubbed using the available function mocking features. Different behaviors can be defined for different test cases if required.

##### Samples

_main.bal_
```ballerina
public function intAdd(int a, int b) returns (int) {
    return a + b;
}
```

_test.bal_

The following example shows different ways of stubbing a function call.

```ballerina
@test:MockFn { functionName: "intAdd" }
test:MockFunction intAddMockFn = new();

public function mockIntAdd(int x, int y) returns int {
    return x - y;
}

@test:Config {}
public function testMockCall() {
    // stubbing to invoke the created mock function
    test:when(intAddMockFn).call("mockIntAdd");
    test:assertEquals(intAdd(10, 6), 4);
    
    // stubbing to return the specified value
    test:when(intAddMockFn).thenReturn(5);
    test:assertEquals(intAdd(10, 4), 5);
    
    // stubbing to return a value based on input
    test:when(intAddMockFn).withArguments(20, 14).thenReturn(100);
    test:assertEquals(intAdd(20, 14), 100);
}
```

### Object Mocking

Object mocking enables controlling the values of member variables and the behavior of the member functions of an object. This is vital when working with client objects as the remote functions can be stubbed to return a mock value without having to actually make calls to the remote endpoint.

Mocking of objects can be done in two ways.The available features provide the functionality to substitute the real object with a user-defined mock object or to stub the behavior of required functions. 

1. Creating a test double (providing an equivalent object in place of the real)
2. Stubbing the member function or member variable (specifying the behavior of functions and values of variables)

Creating a test double is suitable when a single mock function/object can be used throughout all tests whereas stubbing is ideal when defining different behaviors for different test cases is required.

#### Creating a test double

A custom mock object can be defined in place of the real object which should contain the member variables and functions that need to be replaced. The custom object should be made structurally equivalent to the real object via the mocking features in the test module. A runtime exception will be thrown if any of the defined functions or variables is not equivalent to the counterpart of the original object.

_main.bal_
```ballerina
http:Client clientEndpoint = new("http://petstore.com");

function getPet(string petId) returns Pet | error {
  http:Response|error result = clientEndpoint->get("/pets?id="+petId);
  if(result is error) {
    return result;
  } else {
      Pet pet = constructPetObj(result); 
    return pet;
  }
}
```

_test.bal_
```ballerina
// Mock object definition for http:Client object
public type MockHttpClient client object {
    public remote function get(@untainted string path, public 
        http:RequestMessage message = ()) returns http:Response|http:ClientError {
        http:Response res = new;
        res.statusCode = 500;
        return res;
    }
};

@test:Config {}
function testGetPet() {
    //mock object that would act as the test double to the clientEndpoint
    clientEndpoint = <http:Client>mock(http:Client, new MockHttpClient());
    http:Response res = getPet("D123");
    test:assertEquals(res.statusCode, 500);
}
```

#### Stubbing member functions and variables of an object

The member functions and variables are stubbed to return a specific value or to do nothing when invoked. Using the test module, a default mock object of the specified type. The default action of any member function/variable is to panic upon invocation/retrieval.  Subsequent to mock object creation, the required functions and variables of the default mock object should be stubbed to return a value or to do nothing when called.

##### Samples

Example

Following example shows different ways of stubbing an object member functions.
```ballerina

@test:Config {}
function testGetPet2() {
    clientEndpoint = <http:Client>mock(http:Client);

    // stubbing to return the specified value 
    test:prepare(clientEndpoint).when(“get”).thenReturn(new http:Response());
    
    // stubbing to return different values for each function call 
    test:prepare(mockHttpClient).when("get")
        .thenReturnSequence(new http:Response(), mockResponse);
    
    // stubbing to return a value based on input
    test:prepare(mockHttpClient).when("get").withArguments("/pets?id=D123", test:ANY)
        .thenReturn(mockResponse);
    
    // stubbing to do nothing when function is called
    smtpClient=<email:SmtpClient>mock(email:SmtpClient);
    test:prepare(mockSmtpCl).when(“send”).doNothing();

    // add assertions
}
```

The following example shows how to return a value when a member variable is accessed
```ballerina
@test:Config {}
function testMemberVariable() {
  clientEndpoint = <http:Client>test:mock(http:Client);
  test:prepare(clientEndpoint).getMember("url").thenReturn("https://foo.com/");
  test:assertEquals(clientEndpoint.url, "https://foo.com/");
}
```
