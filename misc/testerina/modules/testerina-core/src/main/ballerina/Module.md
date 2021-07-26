## Module Overview

This module facilitates writing tests for Ballerina code in a simple manner. It provides a number of capabilities such as configuring the setup and cleanup steps at different levels, ordering and grouping of tests, providing value-sets to tests, and independence from external functions and endpoints via mocking capabilities.

## Annotations
A Ballerina testsuite can be implemented using a set of annotations. The available annotations enable executing instructions before and after the testsuite or a single test, organize a set of tests into a group, define data-driven tests, specify an order of execution, disable tests and mocking.

### Test Config 

The following example shows a simple testsuite.
```ballerina
import ballerina/test;

// Test function
@test:Config {}
function testFunction() {
    test:assertTrue(true, msg = "Failed!");
}
```

The `before` and `after` attributes can be used to set the execution order by specifying the functions to run before and/or after the test.

```ballerina
import ballerina/io;
import ballerina/test;

function beforeFunc() {
    io:println("I'm the before function!");
}

@test:Config {
    before: beforeFunc,
    after: afterFunc
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}

function afterFunc() {
    io:println("I'm the after function!");
}
```

The `dependsOn` attribute can also be used to set the test execution order by specifying the list of functions that the test function depends on.

```ballerina
import ballerina/io;
import ballerina/test;

@test:Config { 
    dependsOn: [testFunction2] }
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed!");
}

@test:Config {}
function testFunction2() {
    io:println("I'm in test function 2!");
    test:assertTrue(true, msg = "Failed!");
}
```

The `dataProvider` attribute can be used to assign a function to act as a data provider for a test.
The data provider can return data as a map of tuples or as an array of arrays. If there is an issue
 while generating the data set, it can return an error which will be handled by the test framework.

```ballerina
function mapDataProviderTest(int value1, int value2, string fruit) returns error? {
    test:assertEquals(value1, value2, msg = "The provided values are not equal");
    test:assertEquals(fruit.length(), 6);
}

// The data provider function, which returns a  data set as a map of tuples.
function mapDataProvider() returns map<[int, int, string]>|error {
    map<[int, int, string]> dataSet = {
        "banana": [10, 10, "banana"],
        "cherry": [5, 5, "cherry"]
    };
    return dataSet;
}
```

### Before and After Suite

The `BeforeSuite` annotation is used to execute a particular function before the test suite is executed. This can be used to setup the prerequisites before executing the test suite. 

```ballerina
@test:BeforeSuite
function beforeSuit() {
    // initialize or execute pre-requisites
}

```

The `AfterSuite` annotation is used to execute a particular function after the test suite is executed. This is used to tear-down the prerequisites or execute post actions after executing the test suite.

```ballerina
@test:AfterSuite
function afterSuit() {
    // tear-down
}

```

### Before and After Groups

The `BeforeGroups` and annotation can be used to execute the function before the first test function belonging the specified groups.

```ballerina
import ballerina/io;
import ballerina/test;

@test:BeforeGroups { value:["g1"] }
function beforeGroupsFunc() {
    io:println("I'm the before groups function!");
}

@test:Config { groups: ["g1"]}
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed!");
}
```

The `AfterGroups` and annotation can be used to execute the function after the last test function belonging the specified groups.

```ballerina
import ballerina/io;
import ballerina/test;

@test:AfterGroups { value:["g1"] }
function afterGroupsFunc() {
    io:println("I'm the after groups function!");
}

@test:Config { groups: ["g1"]}
function testFunction1() {
    io:println("I'm in test function 1!");
    test:assertTrue(true, msg = "Failed!");
}
```


## Assertions
This module provides a number of assertions in order to verify the expected behaviour of a piece of code. These assertions can be used to decide if the test is passing or failing based on the condition.

Following sample shows how to use assertions in Testerina.
```ballerina
import ballerina/test;

class Person {
     public string name = "";
     public int age = 0;
     public Person? parent = ();
     private string email = "default@abc.com";
     string address = "No 20, Palm grove";
}

@test:Config{}
function testAssertIntEquals() {
    int answer = 0;
    int a = 5;
    int b = 3;
    answer = intAdd(a, b);
    test:assertEquals(answer, 8, msg = "int values not equal");
}

@test:Config {}
function testAssertNotEqualsString() {
    string s1 = "abc";
    string s2 = "def";
    test:assertNotEquals(s1, s2, msg = "string values are equal");
}

@test:Config {}
function testAssertExactEqualsObject() {
    Person p1 = new;
    Person p2 = p1;
    test:assertExactEquals(p1, p2, msg = "Objects are not exactly equal");
}

@test:Config {}
function testAssertNotExactEqualsObject() {
    Person p1 = new;
    Person p2 = new ();
    test:assertNotExactEquals(p1, p2, msg = "Objects are exactly equal");
}

@test:Config {}
function testAssertTrue() {
    boolean value = true;
    test:assertTrue(value, msg = "AssertTrue failed");
}

@test:Config {}
function testAssertFalse() {
    boolean value = false;
    test:assertFalse(value, msg = "AssertFalse failed");
}

@test:Config {}
function testAssertFail() {
    if (true) {
        return;
    }
    test:assertFail(msg = "AssertFailed");
}

function intAdd(int a, int b) returns (int) {
    return (a + b);
}
```

## Mocking

The test module provides capabilities to mock a function or an object for unit testing. The mocking features can be used to control the behavior of functions and objects by defining return values or replacing the entire object or function with a user-defined equivalent. This feature will help you to test the Ballerina code independently from other modules and external endpoints.

### Function Mocking
Function mocking allows to control the behavior of a function in the module being tested or a function of an imported module.

The annotation `@test:Mock {}` is used to declare a `MockFunction` object, with details of the name of the function to be mocked, as well as the module name if an import function is being mocked. The module name value of the annotation is optional if the function being mocked is not an import function.

```ballerina
import ballerina/io;
import ballerina/test;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
test:MockFunction printlnMockFn = new();

int tally = 0;
public function mockPrint(any|error... val) {
    tally = tally + 1;
}

@test:Config {}
function testCall() {
    test:when(printlnMockFn).call("mockPrint");

    io:println("Testing 1");
    io:println("Testing 2");
    io:println("Testing 3");
    test:assertEquals(tally, 3);
}
```

Declaring the annotation with this object will create a default mock object in place of the original function. Subsequent to the declaration, the function call should be stubbed using the available mocking features. Different behaviors can be defined for different test cases if required.

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
import ballerina/test;

@test:Mock { 
    functionName: "intAdd"
}
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
import ballerina/http;

http:Client clientEndpoint = check new ("http://petstore.com");

function getPet(string petId) returns http:Response | error {
  http:Response result = check clientEndpoint->get("/pets?id="+petId);
  return result;
}

function deletePet(string petId) returns error? {
    http:Response res = check clientEndpoint->delete("/pets/delete?id="+petId);
}
```

_test.bal_
```ballerina
import ballerina/test;
import ballerina/http;

// Mock object definition for http:Client object
public client class MockHttpClient {
    remote isolated function get(@untainted string path, map<string|string[]>? headers = (),
    http:TargetType targetType = http:Response) returns http:Response | http:PayloadType | http:ClientError {
        http:Response response = new;
        response.statusCode = 500;
        return response;
    }
}

@test:Config {}
function testTestDouble() returns error? {
    //mock object that would act as the test double to the clientEndpoint
    clientEndpoint = test:mock(http:Client, new MockHttpClient());
    http:Response res = check getPet("D123");
    test:assertEquals(res.statusCode, 500);
}
```

#### Stubbing member functions and variables of an object

The member functions and variables are stubbed to return a specific value or to do nothing when invoked. Using the test module, a default mock object of the specified type. The default action of any member function/variable is to panic upon invocation/retrieval.  After mock object creation, the required functions and variables of the default mock object should be stubbed to return a value or to do nothing when called.

##### Samples

The following example demonstrates how to stub a member function to return a specific value.
```ballerina

@test:Config {}
function testThenReturn() returns error? {
   clientEndpoint = test:mock(http:Client);

    http:Response mockResponse = new;
    mockResponse.statusCode = 300;

    // stubbing to return the specified value 
    test:prepare(clientEndpoint).when("get").thenReturn(mockResponse);

    http:Response res = check getPet("D123");
    test:assertEquals(res.statusCode, 300);
}
```

The following example demonstrates how to stub a member function to return different values on subsequent calls.

```ballerina
@test:Config {}
function testThenReturnSequence() returns error? {
     clientEndpoint = test:mock(http:Client);

    http:Response mockResponse1 = new;
    mockResponse1.statusCode = 400;

    http:Response mockResponse2 = new;
    mockResponse2.statusCode = 401;
    
    // Stubbing to return different values for each function call.
    test:prepare(clientEndpoint).when("get").thenReturnSequence(mockResponse1, mockResponse2);

    http:Response res = check getPet("D123");
    test:assertEquals(res.statusCode, 400);
    res = check getPet("D123");
    test:assertEquals(res.statusCode, 401);
}
```

The following example demonstrates how to stub a member function to do nothing when called.

```ballerina
@test:Config {}
function testDoNothing() returns error? {
    clientEndpoint = test:mock(http:Client);
    
    // Stubbing to return different values for each function call.
    test:prepare(clientEndpoint).when("delete").doNothing();

    error? err = deletePet("D123");
    test:assertEquals(err, ());
}
```

The following example shows how to return a value when a member variable is accessed.
```ballerina
@test:Config {}
function testMemberVariable() {
  clientEndpoint = test:mock(http:Client);
  test:prepare(clientEndpoint).getMember("url").thenReturn("https://foo.com/");
  test:assertEquals(clientEndpoint.url, "https://foo.com/");
}
```
