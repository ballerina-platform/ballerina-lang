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
assertEquals(Any actual, Any expected, string message[Optional])
````
Asserts that the actual is equal to the expected, with an optional message.

````ballerina
assertNotEquals(Any actual, Any expected, string message[Optional])
````
Asserts that the actual is not equal to the expected, with an optional message.

````ballerina
assertFail(string message)
````
Fails the test.

### Mocks
Testerina allows you to create mocks or doubles at different layers for testing your application in the most efficient way. Using testerina we can create,

#### Function Mocks

With Function mocks we can replace a function interface from the same package or from a different package with a mock function. Function mock will look like something below,

````ballerina
@Description {value:"This is a mock function"}
@test:Mock {
    packageName:"src.persistence",
    functionName:"addNewEvent"
}
function mockAddNewEvent (mod:Event evnt) returns json {

    err = {message:"Error"};
    json jsonResponse = { "Success":"Created", "id":"2" };
    return jsonResponse;
}
````

#### Service Mocks

Service mocks allow you to create your own service to mock the actual bach ends.  

Service mock will look something like below,

````ballerina
import ballerina/net.http;

endpoint http:Listener paymentGWEP {
port:9094
};

@http:ServiceConfig {
      endpoints:[paymentGWEP], basePath:"/boc"
}
service<http:Service> PaymentService bind paymentGWEP {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/payment"
    }

     creditOperations (endpoint conn, http:Request req, string eventID) {
        // Expects an API Token
        http:Response res = {};

        json jsonRes = {"Payment":"Sucess!"};
        res.statusCode = 200;
        res.setJsonPayload(jsonRes);
        _ = conn -> respond(res);
    }
}
````

The above mock service can be started as shown below,

````
test:startServices(<Package Name>);
````

### Helper Functions

Helper functions allow you to control ballerina tests, services etc. Currently Testerina has the following helper functions.

#### Data providers
Testerina natively support data driven testing. You can execute the same test function repetitively on distinct data sets by using data-providers. 

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

ballerina ``test your_package --groups unit``

Note: You can also use `--disable-groups` flag to exclude groups from executing. Also un-grouped tests will be added to a group named default.

You can also list available groups with the `--list-groups` flag.
````
e.g : ballerina test --list-groups
````
 
## Writing ballerina tests

- Test can reside in any valid ballerina package. As a Best practice we put them into a subdirectory called tests within a package  
- Test file names and functions can have any name.  
- We can execute all the tests or tests belonging to a specific package..

- If at least one assert fails, whole test function will be marked as failed.
- Detailed information is shown in the test result summary.  
> One package may contain more than one test file.

## Running ballerina tests

Tests can be execute as shown below

```./ballerina test <package_name> [FLAGS]``` Execute tests within a given package

or

```./ballerina test [FLAGS]``` Executes all the tests within packages