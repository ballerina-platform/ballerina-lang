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

import ballerina/java;

# Represents the placeholder to be given for object or record type arguments
public const ANY = "__ANY__";

# Represents the reason for the mock object related errors.
public const INVALID_OBJECT_ERROR = "InvalidObjectError";
public type InvalidObjectError distinct error;

# Represents the reason for the non-existing member function related errors.
public const FUNCTION_NOT_FOUND_ERROR = "FunctionNotFoundError";
public type FunctionNotFoundError distinct error;

# Represents the reason for the function signature related errors.
public const FUNCTION_SIGNATURE_MISMATCH_ERROR = "FunctionSignatureMismatchError";
public type FunctionSignatureMismatchError distinct error;

# Represents the reason for the object member field related errors.
public const INVALID_MEMBER_FIELD_ERROR = "InvalidMemberFieldError";
public type InvalidMemberFieldError distinct error;

# Represents the reason for function mocking related errors.
public const FUNCTION_CALL_ERROR = "FunctionCallError";
public type FunctionCallError distinct error;

# Represents mocking related errors
public type Error InvalidObjectError|FunctionNotFoundError|FunctionSignatureMismatchError|InvalidMemberFieldError|FunctionCallError;

# Prepares a provided default mock object for stubbing.
#
# + mockObject - created default mock object
# + return - prepared object that allows a member function/field to register stubs
public isolated function prepare(object {} mockObject) returns MockObject {
    Error? result = validatePreparedObjExt(mockObject);
    if (result is Error) {
        panic result;
    }
    MockObject obj = new MockObject(mockObject);
    return obj;
}

# Represents a Mock object in which to create stubs for member functions and variables
public class MockObject {
    object {} mockObject;
    string fieldName = "";

    # Gets invoked during the mock object preparation.
    #
    # + mockObject - object to register stubbing
    public isolated function init(object{} mockObject) {
        self.mockObject = mockObject;
    }

    # Allows a member function to stub.
    #
    # + functionName - function name to allow stubbing
    # + return - object that allows stubbing calls to provided member function
    public isolated function when(string functionName) returns MemberFunctionStub {
        Error? result = validateFunctionNameExt(java:fromString(functionName), self.mockObject);
        if (result is Error) {
             panic result;
        }
        MemberFunctionStub mockObjCaseMemFunc = new MemberFunctionStub(self.mockObject);
        mockObjCaseMemFunc.functionName = functionName;
        return mockObjCaseMemFunc;
    }

    # Allows a member variable to stub
    #
    # + fieldName - field name to allow stubbing
    # + return - object that allows stubbing retrieval of provided member variable
    public isolated function getMember(string fieldName) returns MemberVariableStub {
        self.fieldName = fieldName;
        Error? result = validateFieldNameExt(java:fromString(fieldName), self.mockObject);
        if (result is Error) {
             panic result;
        }
        MemberVariableStub memberVariableStub = new MemberVariableStub(self.mockObject);
        memberVariableStub.fieldName = fieldName;
        return memberVariableStub;
    }
}

# Represents an object that allows stubbing member function invocations.
#
# + mockObject - created mock object
# + functionName - member function name
# + args - arguments list of the function
# + returnValue - value to return
# + returnValueSeq - equence of values to return
public class MemberFunctionStub {
    object {} mockObject;
    string functionName = "";
    anydata|error args = [];
    any|error returnValue = ();
    any|error returnValueSeq = [];

    # Gets invoked during the stub registration.
    #
    # + mockObject - object to register
    public isolated function init(object{} mockObject) {
        self.mockObject = mockObject;
    }

    # Sets the arguments list to consider when stubbing the function call.
    #
    # + args - arguments list
    # + return - object that allows stubbing calls to provided member function
    public isolated function withArguments(anydata|error... args) returns MemberFunctionStub {
        self.args = args;
        Error? result = validateArgumentsExt(self);
        if (result is Error) {
            panic result;
        }
        return self;
    }

    # Sets the value to be returned when the function is called.
    #
    # + returnValue - value or error to return
    public isolated function thenReturn(any|error returnValue) {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        self.returnValue = returnValue;
        Error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is Error) {
            panic thenReturnExtResult;
        }
    }

    # Sets the values to be returned when the function is called repeatedly.
    #
    # + returnValues - value or error to return
    public isolated function thenReturnSequence(any|error... returnValues) {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        if (self.args != []) {
            error err = error("'withArguments' function cannot be specified with a return sequence");
            panic err;
        }
        self.returnValueSeq = returnValues;
        Error? thenReturnSeqExtResult = thenReturnSeqExt(self);
        if (thenReturnSeqExtResult is Error) {
            panic thenReturnSeqExtResult;
        }
    }

    # Sets the function behavior to do nothing when called.
    public isolated function doNothing() {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        self.returnValue = ();
        Error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is Error) {
            panic thenReturnExtResult;
        }
    }
}

# Represents an object that allows stubbing member variables retrieved.
#
# + mockObject - created mock object
# + returnValue - value to return
public class MemberVariableStub {
    object {} mockObject;
    any|error returnValue = ();
    string fieldName = "";

    # Gets invoked during the stub registration
    #
    # + mockObject - object to register
    public isolated function init(object{} mockObject) {
        self.mockObject = mockObject;
    }

    # Sets the value to be returned when the function is called.
    #
    # + returnValue - value or error to return
    public isolated function thenReturn(any|error returnValue) {
        if (self.fieldName == "") {
             error err = error("field name is not specified.");
             panic err;
        }
        self.returnValue = returnValue;
        Error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is Error) {
            panic thenReturnExtResult;
        }
    }
}

# Objects and functions related to function mocking

# Allows a function to stub.
#
# + mockFunction - function name to allow stubbing
# + return - object that allows stubbing calls to provided function
public isolated function when(MockFunction mockFunction) returns FunctionStub {
    FunctionStub stub = new FunctionStub(mockFunction);
    return stub;
}

# Represents a MockFunction object
public class MockFunction {
    string functionToMock = "";
    string functionToMockPackage = "";
}

# Represents an object that allows stubbing function invocations
#
# + mockFuncObj - associated mockFunctionObj
# + returnValue - return value
# + args - function arguments
public class FunctionStub {
    MockFunction mockFuncObj;
    any|error returnValue = ();
    anydata|error args = [];


    # Gets invoked during the stub registration
    #
    # + mockObject - object to register
    public isolated function init(MockFunction mockFunction) {
        self.mockFuncObj = mockFunction;
    }

    # Sets the value to be returned when the function is called.
    #
    # + returnValue - value or error to return
    public isolated function thenReturn(any|error returnValue) {
        self.returnValue = returnValue;
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }

    # Sets the arguments list to consider when stubbing the function call.
    #
    # + args - arguments list
    # + return - object that allows stubbing calls to a function
    public isolated function withArguments(anydata|error... args) returns FunctionStub {
        self.args = args;
        return self;
    }

    # Sets the function behavior to do nothing when called
    public isolated function doNothing() {
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }

    # Sets a function to be invoked when the real function is called.
    #
    # + functionName - mock function to call in place of the real
    public isolated function call(string functionName) {
        self.returnValue = "__CALL__" + functionName;
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }

    # Sets the original function to be invoked.
    public isolated function callOriginal() {
        self.returnValue = "__ORIGINAL__";
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }
}

# Creates and returns a mock object of provided type description.
#
# + T - type of object to create the mock
# + mockObject - mock object to replace the original (optional)
# + return - created mock object or throw an error if validation failed
public isolated function mock(typedesc<object{}> T, object{} mockObject = object { }) returns T = @java:Method {
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the mock object.
#
# + mockObject - mock object
# + return - Return Value Description
isolated function validatePreparedObjExt(object{} mockObject) returns Error? = @java:Method {
    name: "validatePreparedObj",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the provided function name
#
# + functionName - function name provided
# + mockObject - object to validate against
# + return - error if function does not exist or in case of a signature mismatch
isolated function validateFunctionNameExt(handle functionName, object{} mockObject) returns Error? = @java:Method {
    name: "validateFunctionName",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the field name.
#
# + fieldName - field name provided
# + mockObject - obj to validate against
# + return - error if field does not exist
isolated function validateFieldNameExt(handle fieldName, object{} mockObject) returns Error? = @java:Method {
    name: "validateFieldName",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the arguments list.
#
# + case - case to validate
# + return - error in case of an argument mismatch
isolated function validateArgumentsExt(MemberFunctionStub case) returns Error? = @java:Method {
    name: "validateArguments",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to register the return value
#
# + case - case to register
# + return - error if case registration failed
isolated function thenReturnExt(MemberFunctionStub|MemberVariableStub case) returns Error? = @java:Method {
    name: "thenReturn",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to register the sequence of return values;
#
# + case - case to register
# + return - error if case registration failed
isolated function thenReturnSeqExt(MemberFunctionStub case) returns Error? = @java:Method {
    name: "thenReturnSequence",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to register return value
#
# + case - case to register
# + return - error if case registration failed
isolated function thenReturnFuncExt(FunctionStub case) returns Error? = @java:Method {
    name: "thenReturn",
    'class: "org.ballerinalang.testerina.natives.mock.FunctionMock"
} external;

# Inter-op to handle function mocking.
#
# + mockFunction - mockFunction object
# + args - function arguments
# + return - function return value or error if case registration failed
public isolated function mockHandler(MockFunction mockFunction, (any|error)... args) returns any|Error = @java:Method {
    name: "mockHandler",
    'class: "org.ballerinalang.testerina.natives.mock.FunctionMock"
} external;
