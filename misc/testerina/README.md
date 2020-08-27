# Testerina

Testerina is the built in test framework for Ballerina. Testerina enables developers to write testable code. 
It provides a set of building blocks to help write tests and a set of tools to help test. 
Developers and testers can cover multiple levels of the test pyramid including unit testing, integration testing and end to end testing with the building blocks the framework provides. It provides the flexibility to the programmer or tester to build intelligent tests that suites the domain and application needs. 

The test framework is designed for testing microservices and container native programs implemented in Ballerina.

Testerina will be a part of ```ballerina-tools-<release-version>.zip``` distribution and it provides ```ballerina test``` command.  


## Testerina provides following functions.

### Assertions
Assertions are used to verify the outcome of a ballerina function or a program against the expected outcome. Following are the available assertions in Testerina.

````ballerina
assertTrue(boolean expression, string message[Optional]) 
````
Asserts that the expression is true with an optional message.

````ballerina
assertFalse(boolean expression, string message[Optional])
````
Asserts that the expression is false with an optional message.

````ballerina
assertEquals(Anydata actual, Anydata expected, string message[Optional])
````
Asserts that the actual value is equal to the expected value, with an optional message.

````ballerina
assertNotEquals(Anydata actual, Anydata expected, string message[Optional])
````
Asserts that the actual value is not equal to the expected value, with an optional message.

````ballerina
assertExactEquals(Any actual, Any expected, string message[Optional])
````
Asserts that the actual entity is exactly equal to the expected entity, with an optional message.

````ballerina
assertNotExactEquals(Any actual, Any expected, string message[Optional])
````
Asserts that the actual entity is not exactly equal to the expected entity, with an optional message.

````ballerina
assertFail(string message)
````
Fails the test.

### Mocks
Testerina allows you to create mocks or doubles at different layers for testing your application in the most efficient way. Using testerina we can create,

#### Function Mocks

With Function mocks we can replace a function interface from the same module or from a different module with a mock function. Function mock will look like something below,

````ballerina
# This is a mock function
#
# + evnt - evnt Parameter Description 
# + return - Return Value Description
@test:Mock {
    moduleName:"src.persistence",
    functionName:"addNewEvent"
}
function mockAddNewEvent (mod:Event evnt) returns json {
    error err = error("Error");
    json jsonResponse = { "Success":"Created", "id":"2" };
    return jsonResponse;
}
````

### Helper Functions

Helper functions allow you to control ballerina tests, services etc. Currently Testerina has the following helper functions.

#### Data providers
Testerina natively support data driven testing. You can execute the same test function repetitively on distinct data sets by using data-providers. 

e.g:

You can add a data provider based test functions as shown below,
```ballerina
import ballerina/test;
import ballerina/log;

@test:Config {
    dataProvider: "testCalculateDataProvider"
}
function testCalculate(int n1, int n2, int expected) {
    int actual = calculate(n1, n2);
    test:assertEquals(actual, expected, msg = string `Calculation is wrong for n1:${n1} and n2:${n2}!`);
}

function testCalculateDataProvider() returns ((int, int, int)[]) {
    return [(5, 5, 10), (10, 10, 20), (500, 500, 1000), (1000, -2000, -100)];
}
```

#### Test Groups
You can group your test functions and control the execution of tests by specifying the groups of tests you wish to execute.

e.g:

You can group the test functions as shown below,
````ballerina
@test:Config{
    groups:["unit","login_module"]
}
function testFunc () {
test:assertFalse(false, msg = "errorMessage");
}
````

Inorder to execute tests belonging to a selected group. Run the below command.

ballerina ``test your_module --groups unit``

Note: You can also use `--disable-groups` flag to exclude groups from executing. Also un-grouped tests will be added to a group named default.

You can also list available groups with the `--list-groups` flag.
````
e.g : ballerina test --list-groups
````
 
## Writing ballerina tests

- Test should reside in a subdirectory called tests within a valid ballerina module.
- Test file names and functions can have any name. As a Best practice we name test file as \<source_name>_tests.  
- We can execute all the tests or tests belonging to a specific module..

- If at least one assert fails, whole test function will be marked as failed.
- Detailed information is shown in the test result summary.  
> One module may contain more than one test file.

## Running ballerina tests

Tests can be execute as shown below

```./ballerina test <module_name> [FLAGS]``` Execute tests within a given module