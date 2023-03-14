import object_mocking2.mod1;
import ballerina/test;

@test:Mock {
    functionName: "initializeHttpClient"
}
function initializeHttpClientMock() returns mod1:HttpClient|error {
    return test:mock(mod1:HttpClient, new MockHttpClient());
}

@test:Config
function testFn() returns error? {
    test:assertEquals(check fn(), "Mock hello from RESOURCE method");
}

public client class MockHttpClient {

    resource function get [string ...path]() returns string {
        return "Mock hello from RESOURCE method";
    }
}
