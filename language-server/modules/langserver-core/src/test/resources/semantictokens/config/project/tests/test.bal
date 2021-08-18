import ballerina/test;
import ballerina/http;
import ballerina/io;

@test:Config {}
function testFunc1() returns @tainted error? {
    // Invoking the main function.
    http:Client httpEndpoint = checkpanic new ("http://localhost:9090");

    // Send a `POST` request to the specified endpoint.
    json payload = {"hello": "world"};
    var response = httpEndpoint->post("/foo/bar", payload);
    if (response is http:Response) {
        var jsonRes = check response.getJsonPayload();
        test:assertEquals(jsonRes, payload);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}

@test:Mock {
    moduleName: "ballerina/time",
    functionName: "currentTime"
}
string[] output = [];
test:MockFunction currentTime = new ();

function toString(any|error val) returns string => val is error ? val.toString() : val.toString();

public function mockPrint(any|error... val) {
    output.push(
    toString(val.reduce(function(any|error a, any|error b) returns string => toString(a) + toString(b), "")));
}

@test:Config {}
function testFunc2() {
    test:when(currentTime).call("mockCurrentTime");
    test:assertEquals(output[0], "Full name: John Doe");
    test:assertEquals(output[1], "{\"time\":1577854800000,\"zone\":{\"id\":\"America/Panama\",\"offset\":-18000}}");

    string[] responseMsgs = [];
    int waitCount = 0;
    boolean completed = false;
    while (true) {
        if (waitCount > 10) {
            break;
        }
        waitCount += 1;
    }
    test:assertEquals(completed, true, msg = "Incomplete response message.");
    string expectedMsg1 = "Hi Sam";
    string expectedMsg2 = "Hey Sam";
    string expectedMsg3 = "GM Sam";
    foreach string msg in responseMsgs {
        test:assertTrue(msg == expectedMsg1 || msg == expectedMsg2 || msg == expectedMsg3);
    }
}

@test:BeforeSuite
function beforeSuit() {
    io:println("I'm the before suite function!");
}

@test:AfterSuite {}
function afterSuite() {
    io:println("I'm the after suite function!");
}

@test:BeforeGroups {value: ["g1"]}
function beforeGroupsFunc() {
    io:println("I'm the before groups function!");
}

// The after-groups function, which is executed after
// the last test function belonging to the group `g1`.
@test:AfterGroups {value: ["g1"]}
function afterGroupsFunc() {
    io:println("I'm the after groups function!");
}

@test:BeforeEach
function beforeEachFunc() {
    io:println("I'm the before each function!");
}

// The after-each function, which is executed after each test function.
@test:AfterEach
function afterEachFunc() {
    io:println("I'm the after each function!");
}

@test:Config {
    before: testFunc1,
    after: testFunc1
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}
