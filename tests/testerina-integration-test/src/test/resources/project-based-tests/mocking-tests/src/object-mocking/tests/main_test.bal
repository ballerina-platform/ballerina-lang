import ballerina/test;
import TestHttpClient;

// Mock Object definition
public client class MockHttpClient {

    public string url = "http://mockUrl";

    public remote function get(string path) returns string {
        return self.url + path + "/mocked";
    }
}

@test:Config {}
function testUserDefinedMockObject() {
    clientEndpoint = test:mock(TestHttpClient:HttpClient, new MockHttpClient());

    string response = doGet();
    test:assertEquals(response, "http://mockUrl/path1/mocked");
}

@test:Config {}
function testProvideAReturnValue() {
    TestHttpClient:HttpClient mockClient = test:mock(TestHttpClient:HttpClient);

    test:prepare(mockClient).when("get").thenReturn("provided return value");
    clientEndpoint = mockClient;

    string response = doGet();
    test:assertEquals(response, "provided return value");
}

@test:Config {}
function testProvideAReturnValueBasedOnInput() {
    TestHttpClient:HttpClient mockClient = test:mock(TestHttpClient:HttpClient);


    test:prepare(mockClient).when("get").withArguments("/path1").thenReturn("returning value based on input");
    clientEndpoint = mockClient;

    string response = doGet();
    test:assertEquals(response, "returning value based on input");
}

@test:Config {}
function testMockMemberVariable() {
    string mockClientUrl = "http://foo";

    TestHttpClient:HttpClient mockClient = test:mock(TestHttpClient:HttpClient);
    test:prepare(mockClient).getMember("url").thenReturn(mockClientUrl);
    clientEndpoint = mockClient;

    test:assertEquals(getClientUrl(), mockClientUrl);
}

@test:Config {}
function testProvideAReturnSequence() {
    TestHttpClient:HttpClient mockClient = test:mock(TestHttpClient:HttpClient);

    test:prepare(mockClient).when("get").thenReturnSequence("response1", "response2");
    clientEndpoint = mockClient;

    test:assertEquals(doGetRepeat(), "response2");
}



