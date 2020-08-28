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
import ballerina/http;
//import ballerina/email;

// Mock object definition
public type MockHttpClient client object {
  public string url = "http://mockUrl";

  public remote function get(@untainted string path, public http:RequestMessage message = ()) returns http:Response|http:ClientError {
      http:Response res = new;
      res.statusCode = 500;
      return res;
  }

};

@test:Config {}
function testUserDefinedMockObject() {

  clientEndpoint = test:mock(http:Client, new MockHttpClient());
  http:Response res = doGet();
  test:assertEquals(res.statusCode, 500);
  test:assertEquals(getClientUrl(), "http://mockUrl");
}

@test:Config {}
function testProvideAReturnValue() {

  http:Client mockHttpClient = test:mock(http:Client);
  http:Response mockResponse = new;
  mockResponse.statusCode = 500;

  test:prepare(mockHttpClient).when("get").thenReturn(mockResponse);
  clientEndpoint = mockHttpClient;
  http:Response res = doGet();
  test:assertEquals(res.statusCode, 500);
}

@test:Config {}
function testProvideAReturnValueBasedOnInput() {

  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).when("get").withArguments("/get?test=123", test:ANY).thenReturn(new http:Response());
  clientEndpoint = mockHttpClient;
  http:Response res = doGet();
  test:assertEquals(res.statusCode, 200);
}

//@test:Config {}
//function testProvideErrorAsReturnValue() {
//
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient);
//  smtpClient = mockSmtpClient;
//
//  string[] emailIds = ["user1@test.com", "user2@test.com"];
//  error? errMock = email:SendError("email sending failed");
//  test:prepare(mockSmtpClient).when("send").thenReturn(errMock);
//  error? err = sendNotification(emailIds);
//  test:assertTrue(err is error);
//}
//
//@test:Config {}
//function testDoNothing() {
//
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient);
//  http:Response mockResponse = new;
//  mockResponse.statusCode = 500;
//
//  test:prepare(mockSmtpClient).when("send").doNothing();
//  smtpClient = mockSmtpClient;
//
//  string[] emailIds = ["user1@test.com", "user2@test.com"];
//  error? err = sendNotification(emailIds);
//  test:assertEquals(err, ());
//}

@test:Config {}
function testMockMemberVarible() {
  string mockClientUrl = "http://foo";
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).getMember("url").thenReturn(mockClientUrl);

  clientEndpoint = mockHttpClient;
  test:assertEquals(getClientUrl(), mockClientUrl);
}

@test:Config {}
function testProvideAReturnSequence() {
    http:Client mockHttpClient = test:mock(http:Client);
    http:Response mockResponse = new;
    mockResponse.statusCode = 500;

    test:prepare(mockHttpClient).when("get").thenReturnSequence(new http:Response(), mockResponse);
    clientEndpoint = mockHttpClient;
    http:Response res = doGetRepeat();
    test:assertEquals(res.statusCode, 500);
}

# VALIDATION CASES
# 1 - Validations for user defined mock object

public type MockSmtpClientEmpty client object {};

//public type MockSmtpClient client object {
//  public remote function send(email:Email email) returns email:Error?  {
//     // do nothing
//  }
//};
//
//public type MockSmtpClientFuncErr client object {
//  public remote function sendMail(email:Email email) returns email:Error?  {
//      // do nothing
//  }
//};
//
//public type MockSmtpClientSigErr client object {
//  public remote function send(email:Email email) returns string {
//    return "";
//  }
//};
//
//public type MockSmtpClientSigErr2 client object {
//  public remote function send(string[] email) returns string {
//    return "";
//  }
//};

public type MockHttpClientSigErr client object {
  public remote function get(@untainted string path, any message = ()) returns http:Response|http:ClientError {
      http:Response res = new;
      res.statusCode = 500;
      return res;
  }
};

//// 1.1) when the user-defined mock object is empty
//@test:Config {}
//function testEmptyUserDefinedObj() {
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient, new MockSmtpClientEmpty());
//  smtpClient = mockSmtpClient;
//}
//
//
//// 1.2) when user-defined object is passed to test:prepare function
//@test:Config {}
//function testUserDefinedMockRegisterCases() {
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient, new MockSmtpClient());
//  test:prepare(mockSmtpClient).when("send").doNothing();
//}
//
//// 1.3) when the functions in mock is not available in the original
//@test:Config {}
//function testUserDefinedMockInvalidFunction() {
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient, new MockSmtpClientFuncErr());
//  smtpClient = mockSmtpClient;
//  error? sendNotificationResult = sendNotification(["user1@test.com"]);
//}
//
//// 1.4.1) when the function return types do not match
//@test:Config {}
//function testUserDefinedMockFunctionSignatureMismatch() {
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient, new MockSmtpClientSigErr());
//  smtpClient = mockSmtpClient;
//  error? sendNotificationResult = sendNotification(["user1@test.com"]);
//}
//
//// 1.4.2) when the function parameters do not match
//@test:Config {}
//function testUserDefinedMockFunctionSignatureMismatch2() {
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient, new MockSmtpClientSigErr2());
//  smtpClient = mockSmtpClient;
//  error? sendNotificationResult = sendNotification(["user1@test.com"]);
//}

// 1.4.3
@test:Config {}
function testUserDefinedMockFunctionSignatureMismatch3() {
  http:Client mockHttpClient = test:mock(http:Client, new MockHttpClientSigErr());
}

# 2 - Validations for framework provided default mock object

//// 2.1  when the function called in mock is not available in the original
//@test:Config {}
//function testDefaultMockInvalidFunctionName() {
//  email:SmtpClient mockSmtpClient = test:mock(email:SmtpClient);
//  test:prepare(mockSmtpClient).when("get").doNothing();
//}

// 2.2) call doNothing() - the function has a return type specified
@test:Config {}
function testDefaultMockWrongAction() {
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).when("get").doNothing();
}

// 2.3) when the return value does not match the function return type
@test:Config {}
function testDefaultInvalidFunctionReturnValue() {
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).when("get").thenReturn("success");
}

// 2.4.1) when the number of arguments provided does not match the function signature
@test:Config {}
function testDefaultTooManyArgs() {
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).when("get").withArguments("test", "", "").thenReturn(new http:Response());
}

// 2.4.2) when the type of arguments provided does not match the function signature
@test:Config {}
function testDefaultIncompatibleArgs() {
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).when("get").withArguments(0).thenReturn(new http:Response());
}

// 2.5) when the object does not have a member variable of specified name
@test:Config {}
function testDefaultMockInvalidFieldName() {
  string mockClientUrl = "http://foo";
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).getMember("clientUrl").thenReturn(mockClientUrl);

  clientEndpoint = mockHttpClient;
  test:assertEquals(getClientUrl(), mockClientUrl);
}

// 2.6) when the member variable type does not match the return value
@test:Config{}
function testDefaultInvalidMemberReturnValue() {
  http:Client mockHttpClient = test:mock(http:Client);
  test:prepare(mockHttpClient).getMember("url").thenReturn(());
}
