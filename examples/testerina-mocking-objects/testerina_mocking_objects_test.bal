// This demonstrates different ways to mock a client object.
import ballerina/test;
import ballerina/http;
import ballerina/email;

// This is the test double of the `http:Client` object with the
// implementation of the required functions.
public type MockHttpClient client object {
    public remote function get(@untainted string path,
        public http:RequestMessage message = ()) returns
            http:Response|http:ClientError {

        http:Response res = new;
        res.statusCode = 500;
        return res;
    }
};

@test:Config {}
function testTestDouble() {
    // This creates and assigns the defined test-double.
    clientEndpoint = test:mock(http:Client, new MockHttpClient());
    http:Response res = performGet();
    test:assertEquals(res.statusCode, 500);
}

@test:Config {}
function testReturn() {
    // This creates and assigns a default mock object, which needs to be stubbed subsequently.
    clientEndpoint = test:mock(http:Client);
    // This stubs the `get` function to return the specified HTTP response.
    test:prepare(clientEndpoint).when("get").thenReturn(new http:Response());
    http:Response res = performGet();
    test:assertEquals(res.statusCode, 200);
}

@test:Config {}
function testReturnWithArgs() {
    http:Response mockResponse = new;
    mockResponse.statusCode = 404;

    clientEndpoint = test:mock(http:Client);
    // This stubs the `get` function to return the specified HTTP response
    // when the specified argument is passed.
    test:prepare(clientEndpoint).when("get").withArguments("/headers")
        .thenReturn(mockResponse);
    // The object and record types should be denoted by the `test:ANY` constant
    test:prepare(clientEndpoint).when("get")
        .withArguments("/get?test=123", test:ANY).thenReturn(mockResponse);
    http:Response res = performGet();
    test:assertEquals(res.statusCode, 404);
}

@test:Config {}
function testReturnSequence() {
    http:Response mockResponse = new;
    mockResponse.statusCode = 404;

    clientEndpoint = test:mock(http:Client);
    // This stubs the `get` function to return the specified HTTP response
    // for each call (i.e., the first call will return the status code `200`
    // and the second call will return the status code `404`).
    test:prepare(clientEndpoint).when("get")
        .thenReturnSequence(new http:Response(), mockResponse);
    http:Response res = performGet();
    test:assertEquals(res.statusCode, 404);
}

@test:Config {}
function testSendNotification() {
    smtpClient = test:mock(email:SmtpClient);
    // This stubs the `send` method of the `mockSmtpClient` to do nothing.
    // This is used for functions with an optional or no return type
    test:prepare(smtpClient).when("send").doNothing();
    string[] emailIds = ["user1@test.com", "user2@test.com"];
    error? err = sendNotification(emailIds);
    test:assertEquals(err, ());

}

@test:Config {}
function testMemberVariable() {
    string mockClientUrl = "http://foo";
    clientEndpoint = test:mock(http:Client);
    // This stubs the value of the `url` to return the specified string.
    test:prepare(clientEndpoint).getMember("url").thenReturn(mockClientUrl);
    test:assertEquals(clientEndpoint.url, mockClientUrl);

}
