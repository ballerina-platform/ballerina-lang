// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;
import object_mocking.TestHttpClient;

// Mock Object definition
public client class MockHttpClient {

    public string url = "http://mockUrl";

    remote function get(string path) returns string {
        return self.url + path + "/mocked";
    }
}

public class MockPersonObj {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function name() returns string => self.fname + " " + self.lname;

    public function getValue(string param, typedesc<int|string> td) returns int|string|error => "mock value";
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

@test:Config {
}
function testMockStreamSuccess() {
    clientEndpoint = test:mock(TestHttpClient:HttpClient);  
    test:prepare(clientEndpoint).when("get_stream").thenReturn(returnMockedAttributeDAOStream());
    TestHttpClient:AttributeDAO|error result = getAttribute();
    test:assertEquals(result, mockAttributeDAO);
}

@test:Config {}
function testProvideErrorReturnValue() {
    TestHttpClient:HttpClient mockClient = test:mock(TestHttpClient:HttpClient);

    test:prepare(mockClient).when("getError").thenReturn(error("mocked error response"));
    clientEndpoint = mockClient;

    string|error response = getTheError();
    test:assertTrue(response is error);
    test:assertEquals((<error>response).message(), "mocked error response");
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

@test:Config {}
function testDependentlyTypedFunctions_thenReturn() {
    PersonObj mockPObj = test:mock(PersonObj);
    test:prepare(mockPObj).when("getValue").thenReturn("Testing");

    pObj = mockPObj;

    string stringValue = pObj.getValue("id1", td = string);
    test:assertEquals(stringValue, "Testing");

    test:prepare(mockPObj).when("getValue").withArguments("id2").thenReturn(5);
    int intValue = pObj.getValue("id2", td = int);
    test:assertEquals(intValue, 5);
}

@test:Config {}
function testDependentlyTypedFunctions_testDouble() {
      pObj = test:mock(PersonObj, new MockPersonObj("John", "Doe"));

      var s = pObj.getValue("id3", td = string);
      test:assertEquals(s, "mock value");
}
