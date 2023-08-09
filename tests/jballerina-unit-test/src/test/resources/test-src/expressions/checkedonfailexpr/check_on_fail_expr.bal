// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function returnInt() returns int => 3;

function testCheckedOnFailExprReturnsValue() {
    int val = check returnInt() on fail e => error("Error occurred", e);
    assertEquals(3 , val);

    val = 2 + check returnInt() on fail e => error("Error occurred", e);
    assertEquals(5, val);

    val = check returnInt() on fail e => error("Error occurred", e) is int ? 1 : 0;
    assertEquals(1, val);
}

function returnError1() returns error? {
    return error("Something went wrong");
}

function returnError2() returns int|error {
    return error("Something went wrong");
}

function returnError3() returns error {
    return error("New error");
}

function returnError4(error e) returns error {
    return error("New error", e);
}

function testCheckedOnFailExpressionWithCustomError1() returns error? {
    check returnError1() on fail e => error("Custom error", e);
}

function testCheckedOnFailExpressionWithCustomError2() returns error? {
    int value = check returnError2() on fail e => error("Custom error", e);
}

function testCheckedOnFailWithFunctionReturningError1() returns error? {
    check returnError1() on fail e => returnError3();
}

function testCheckedOnFailWithFunctionReturningError2() returns error? {
    check returnError1() on fail e => returnError4(e);
}

function testCheckedOnFailExpressionWithCustomError() {
    error? err1 = testCheckedOnFailExpressionWithCustomError1();
    assertTrue(err1 is error);
    assertEquals("Custom error", (<error> err1).message());
    assertEquals("Something went wrong", (<error> (<error> err1).cause()).message());

    error? err2 = testCheckedOnFailExpressionWithCustomError2();
    assertTrue(err2 is error);
    assertEquals("Custom error", (<error> err2).message());
    assertEquals("Something went wrong", (<error> (<error> err2).cause()).message());

    error? err3 = testCheckedOnFailWithFunctionReturningError1();
    assertTrue(err3 is error);
    assertEquals("New error", (<error> err3).message());

    error? err4 = testCheckedOnFailWithFunctionReturningError2();
    assertTrue(err4 is error);
    assertEquals("New error", (<error> err4).message());
    assertEquals("Something went wrong", (<error> (<error> err4).cause()).message());
}

function testCheckedOnFailExpressionWithQueryExpression() {
    var val = check from int num in [1, 2, 3] where num >= 2 select num on fail e => error("Error occurred", e);
    assertEquals([2, 3], val);
}

function assertTrue(anydata actual) {
    assertEquals(true, actual);
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
