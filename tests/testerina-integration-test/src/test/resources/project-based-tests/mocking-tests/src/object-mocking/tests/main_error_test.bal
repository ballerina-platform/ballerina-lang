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
import TestHttpClient;

public client class MockHttpClientSigErr {
     public remote function get(int path) returns string {
          return "/error";
     }
}

//@test:Config {}
//function testUserDefinedMockObjectFunctionSignatureMismatch() {
//    TestHttpClient:HttpClient = test:mock(TestHttpClient:HttpClient, new MockHttpClientSigErr());
//}

// when the return value does not match the function return type
@test:Config {}
function testDefaultMockInvalidReturnValue() {
    TestHttpClient:HttpClient mockHttpClient = test:mock(TestHttpClient:HttpClient);
    test:prepare(mockHttpClient).when("get").thenReturn(500);
}

// call doNothing() - the function has a return type specified
@test:Config {}
function testDefaultMockWrongAction() {
    TestHttpClient:HttpClient mockHttpClient = test:mock(TestHttpClient:HttpClient);
    test:prepare(mockHttpClient).when("get").doNothing();
}

// when too many arguements are provided
@test:Config {}
function testDefaultTooManyArgs() {
    TestHttpClient:HttpClient mockHttpClient = test:mock(TestHttpClient:HttpClient);
    test:prepare(mockHttpClient).when("get").withArguments("test", "", "").thenReturn("too many args");
}

// when the type of arguments provided does not match the function signature
@test:Config {}
function testDefaultIncompatibleArgs() {
  TestHttpClient:HttpClient mockHttpClient = test:mock(TestHttpClient:HttpClient);
  test:prepare(mockHttpClient).when("get").withArguments(10.5).thenReturn("invalid args");
}

// when the object doesnt have a member variable of the specified name
@test:Config {}
function testDefaultMockInvalidFieldName() {
     string mockClientUrl = "http://foo";

     TestHttpClient:HttpClient mockClient = test:mock(TestHttpClient:HttpClient);
     test:prepare(mockClient).getMember("invalidField").thenReturn(mockClientUrl);
     clientEndpoint = mockClient;
     test:assertEquals(getClientUrl(), mockClientUrl);
}

// when the member varible type doesnt match the return values
@test:Config {}
function testDefaultInvalidMemberReturnValue() {
    TestHttpClient:HttpClient mockHttpClient = test:mock(TestHttpClient:HttpClient);
    test:prepare(mockHttpClient).getMember("url").thenReturn(());
}
