// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;
import ballerina/lang.'value;
import ballerina/test;

public class Person {
    int age = 9;
    public function init(int age) {
        self.age = age;
    }
}

public type ResourceDefinition record {|
    string path;
    string method;
|};

public type ApiDefinition record {|
    ResourceDefinition[] resources;
|};

public type MyType byte[]|string|int|float;

function testJavaNullPointerException() {
    error? unionResult = trap callThrowNPEWithCallback();
    test:assertEquals(unionResult is error, true);
    if unionResult is error {
        test:assertEquals(unionResult.message(), "java.lang.NullPointerException");
        test:assertEquals(unionResult.stackTrace().toBalString(),
        "[object callableName: throwNPE  fileName: static_method_tests.bal lineNumber: 190," +
        "object callableName: $lambda$_0  fileName: static_method_tests.bal lineNumber: 53]");
    }
}

function callThrowNPEWithCallback() {
    int[] arr = [1, 2, 3];
    arr.forEach(function(int x) {
        throwNPE();
    });
}

function testAcceptNothingAndReturnNothing() {
    acceptNothingAndReturnNothing();
}

function testInteropFunctionWithDifferentName() {
    interopFunctionWithDifferentName();
}

function testAcceptNothingButReturnDate() returns handle {
    return acceptNothingButReturnDate();
}

function testAcceptNothingButReturnString() returns string {
    return acceptNothingButReturnString();
}

function testAcceptSomethingAndReturnSomething(handle h) returns handle {
    return acceptSomethingAndReturnSomething(h);
}

function testAcceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle {
    return acceptTwoParamsAndReturnSomething(h1, h2);
}

function testAcceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle {
    return acceptThreeParamsAndReturnSomething(h1, h2, h3);
}

function testErrorOrTupleReturn() returns error|[string,string] {
   [string,string] ret = check getArrayValue();
   return ret;
}

function testFuncWithAsyncDefaultParamExpression() returns int {
    return funcWithAsyncDefaultParamExpression() + funcWithAsyncDefaultParamExpression(5) + funcWithAsyncDefaultParamExpression(50, 20);
}

function asyncRet() returns int {
    sleep(50);
    return 10;
}

function asyncRetWithVal(int a = 30) returns int {
    sleep(50);
    return a + 20;
}

function testUsingParamValues() returns int {
    return usingParamValues() + usingParamValues(5) + usingParamValues(50, 20);
}

function testDecimalParamAndReturn(decimal a1) returns decimal {
    return decimalParamAndReturn(a1);
}
function testStaticResolve() {
    // When instance and static methods have the same name resolve static method based on the parameter type
    int val = hashCode(2);
    test:assertEquals(val, 2);
}

public function testBalEnvSlowAsyncVoidSig() {
    int added = addTwoNumbersSlowAsyncVoidSig(1, 2);
    test:assertEquals(3, added);
}

public function testUnionReturn() returns string {
    ResourceDefinition resourceDef = {path:"path", method:"method"};
    ResourceDefinition[] resources = [resourceDef];
    ApiDefinition apiDef = {resources:resources};
    return value:toString(checkpanic getMapOrError("swagger", apiDef));
}

public function testBalEnvFastAsyncVoidSig() {
    int added = addTwoNumbersFastAsyncVoidSig(1, 2);
    test:assertEquals(3, added);
}

public function testBalEnvSlowAsync() {
    int added = addTwoNumbersSlowAsync(1, 2);
    test:assertEquals(3, added);
}

public function testBalEnvFastAsync() {
    int added = addTwoNumbersFastAsync(1, 2);
    test:assertEquals(3, added);
}

public function testReturnNullString() {
    string concat = "some" + (returnNullString(true) ?: "");
    test:assertEquals("some", concat);
}

public function testReturnNotNullString() {
    string concat = "some" + (returnNullString(false) ?: "");
    test:assertEquals("someNotNull", concat);
}

public function testStringCast() {
    any result = getValue();
    string s = <string> result;
    test:assertEquals("Ballerina", s);
}

public function testGetCurrentModule() {
     string moduleString =  getCurrentModule(4);
     test:assertEquals(moduleString, "$anon#.#0#4");
}

public function testGetDefaultValueWithBEnv() {
     int defaultValue =  getDefaultValueWithBEnv();
     test:assertEquals(defaultValue, 2021);
}

public function testDefaultDecimalArgsAddition() {
    decimal val = defaultDecimalArgsAddition(5);
    decimal expected = 15.05;
    test:assertEquals(val, expected);
}

public function testDefaultDecimalArgs() {
    handle h = java:fromString("8");
    anydata val = defaultDecimalArgs(h);
    anydata expected = ();
    test:assertEquals(val, expected);
}

function hashCode(int receiver) returns int = @java:Method {
    name: "hashCode",
    'class: "java.lang.Byte",
    paramTypes: ["byte"]
} external;

// Interop functions
function throwNPE() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingAndReturnNothing() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopFunctionWithDifferentName() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptNothingAndReturnNothing"
} external;

public function acceptNothingButReturnDate() returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingButReturnString() returns string = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function stringParamAndReturn(string a1) returns string = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingAndReturnSomething(handle h) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsCheckedException() returns error? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsMultipleCheckedException() returns error? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsUncheckedException() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException() returns error? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsCheckedException() returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsMultipleCheckedException() returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException() returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsUncheckedException() returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(handle h1) returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsCheckedException(handle h1) returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(handle h1) returns handle | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsUncheckedException(handle h1) returns handle = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getMapOrError(string swaggerFilePath, ApiDefinition apiDef) returns ApiDefinition | error  = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function getObjectOrError() returns Person|error = @java:Method {
    name: "returnObjectOrError",
    'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getArrayValue() returns [string, string] | error = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function funcWithAsyncDefaultParamExpression(int a1 = asyncRet(), int a2 = asyncRet()) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function usingParamValues(int a1 = asyncRet(), int a2 = asyncRetWithVal(a1)) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function decimalParamAndReturn(decimal a1) returns decimal = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function addTwoNumbersSlowAsyncVoidSig(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function addTwoNumbersFastAsyncVoidSig(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function addTwoNumbersSlowAsync(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    paramTypes: ["long" ,"long"]
} external;

public function addTwoNumbersFastAsync(int a, int b) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getCurrentModule(int a) returns string  = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getDefaultValueWithBEnv(int a = 2021) returns int  = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function returnNullString(boolean nullVal) returns string? = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getValue() returns MyType = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;



type Details record {
    string name;
    int id;
};

public function testCreateRawDetails() {
    Details val = createRawDetails();
    val.name = "riyafa";
}

function createRawDetails() returns Details = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


public function testCreateDetails() {
    Details val = createDetails();
    val.name = "riyafa";
}

function createDetails() returns Details = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

type Student record {
    string name;
    string birth;
};

public function testCreateStudent() {
    (Student & readonly) val = createStudent();
    test:assertEquals(val.name, "Riyafa");
}

function createStudent() returns (Student & readonly) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function testCreateStudentUsingType() {
    (Student & readonly) val = createStudentUsingType();
}

function createStudentUsingType() returns (Student & readonly) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function defaultDecimalArgsAddition(decimal a, decimal b = 10.05) returns (decimal) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function defaultDecimalArgs(handle s, decimal d = -1) returns (anydata) = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function getStringFromFutureResult() returns string = @java:Method {
    name: "getStringWithBalEnv",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public client isolated class Client {
    resource function get getStringFromFutureResult() returns string = @java:Method {
        name: "getStringWithBalEnv",
        'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;

    resource function get getAnydataFromFutureResult() returns anydata = @java:Method {
        name: "getStringWithBalEnv",
        'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;

    resource function get getIntFromFutureResult() returns anydata = @java:Method {
        name: "getIntWithBalEnv",
        'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;
}

function getMapFromFutureResult(string name, int age, map<anydata> results) returns map<any> = @java:Method {
    name: "getMapValueWithBalEnv",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function testBalEnvAcceptingMethodRetType() {

    string stringResult = getStringFromFutureResult();
    test:assertEquals(stringResult, "Hello World!");

    Client clientResult = new Client();
    stringResult = clientResult->/getStringFromFutureResult();
    test:assertEquals(stringResult, "Hello World!");

    anydata anydataResult = clientResult->/getAnydataFromFutureResult();
    test:assertEquals(anydataResult, "Hello World!");

    anydataResult = clientResult->/getIntFromFutureResult();
    test:assertEquals(anydataResult, 7);

    map<any> mapResult = getMapFromFutureResult("John", 35, {"Mathematics": 99, "Physics": 95});
    test:assertEquals(mapResult, {"name":"John","age":35,"results":{"Mathematics":99,"Physics":95}});
}

type A record {|
    int id;
    string name;
    anydata...;
|};

type B record {|
    int id;
    string age;
    anydata...;
|};

public type U A|B;

public isolated client class ClientObj {
    isolated resource function get orderitem/[string orderId]/[string itemId](typedesc<anydata> targetType = <>)
                                                            returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject",
                                    "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get orderitem/[string id](int i, float f, decimal d, string s,
                                        typedesc<anydata> targetType = <>) returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject",
                                    "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get vendor/[string product]/[string itemId](typedesc<anydata> targetType = <>)
                                                                returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResourceWithBundledParams",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray",
                                                                    "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get vendor(int i, float f, string s, typedesc<anydata> targetType = <>) returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get item/[int id]/[string desc](int i, float f, string s, typedesc<anydata> targetType = <>) returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResourceWithBundledParams",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get abc/[string path1]/[string path2](U u, typedesc<anydata> targetType = <>)
                                                                returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject",
                                    "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get def/[string path1]/[string path2](any a, typedesc<anydata> targetType = <>)
                                                                    returns targetType|error = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject",
                                    "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated function getResourceMethod(service object {} serviceObject, string[] path) returns anydata = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
    } external;
}

public function testBundleFuncArgsToBArray() returns error? {
    ClientObj cl = new;
    anydata res = check cl->/orderitem/["1234"]/["abcd"]();
    test:assertEquals(res, 5);
    res = check cl->/orderitem/["1234"](1, 5.2, 7.4, "123");
    test:assertEquals(res, 5);
    res = check cl->/vendor/["product"]/["itemId"]();
    test:assertEquals(res, 1);
    res = check cl->/vendor(1, 5.2, "123");
    test:assertEquals(res, 10);
    res = check cl->/item/[1]/["asd"](1, 5.2, "123");
    test:assertEquals(res, 1);
    res = check cl->/abc/["1234"]/["abcd"]({id: 1, name: "John"});
    test:assertEquals(res, 5);
    res = check cl->/def/["1234"]/["abcd"](1);
    test:assertEquals(res, 5);
    res = cl.getResourceMethod(isolated service object {}, ["orderitem", "1234", "abcd"]);
    test:assertEquals(res, 1000);
}
