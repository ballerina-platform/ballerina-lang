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

import ballerina/jballerina.java;

# Represents the placeholder to be given for object or record type arguments.
public const ANY = "__ANY__";

# Represents the reason for the mock object related errors.
public const INVALID_OBJECT_ERROR = "InvalidObjectError";
# Represents an error where the object trying to be mocked is not valid.
public type InvalidObjectError distinct error;

# Represents the reason for the non-existing member function related errors.
public const FUNCTION_NOT_FOUND_ERROR = "FunctionNotFoundError";
# Represents an error where the function to be mocked cannot be found.
public type FunctionNotFoundError distinct error;

# Represents the reason for the function signature related errors.
public const FUNCTION_SIGNATURE_MISMATCH_ERROR = "FunctionSignatureMismatchError";
# Represents an error where the mock function contains a signature mismatch with the function to be mocked.
public type FunctionSignatureMismatchError distinct error;

# Represents the reason for the object member field related errors.
public const INVALID_MEMBER_FIELD_ERROR = "InvalidMemberFieldError";
# Represents an error where object member field is invalid.
public type InvalidMemberFieldError distinct error;

# Represents the reason for function mocking related errors.
public const FUNCTION_CALL_ERROR = "FunctionCallError";
# Represents an error during function call for mocking.
public type FunctionCallError distinct error;

# Represents mocking related errors.
public type Error InvalidObjectError|FunctionNotFoundError|FunctionSignatureMismatchError|InvalidMemberFieldError|FunctionCallError;

# Prepares a provided default mock object for stubbing.
#
# + mockObject - Created default mock object
# + return - Prepared object that allows a member function/field to register stubs
public isolated function prepare(object {} mockObject) returns MockObject {
    error? result = validatePreparedObjExt(mockObject);
    if (result is error) {
        panic result;
    }
    MockObject obj = new MockObject(mockObject);
    return obj;
}

# Represents a Mock object in which stubs for member functions and variables need to be specified.
public class MockObject {
    object {} mockObject;
    string fieldName = "";

    # Gets invoked during the mock object preparation.
    #
    # + mockObject - Object to register stubbing
    public isolated function init(object{} mockObject) {
        self.mockObject = mockObject;
    }

    # Allows a member function to stub.
    #
    # + functionName - Function name to allow stubbing
    # + return - Object that allows stubbing calls to provided member function
    public isolated function when(string functionName) returns MemberFunctionStub {
        error? result = validateFunctionNameExt(java:fromString(functionName), self.mockObject);
        if (result is error) {
             panic result;
        }
        MemberFunctionStub mockObjCaseMemFunc = new MemberFunctionStub(self.mockObject);
        mockObjCaseMemFunc.functionName = functionName;
        return mockObjCaseMemFunc;
    }

    # Allows a member variable to stub.
    #
    # + fieldName - Field name to allow stubbing
    # + return - Object that allows stubbing retrieval of provided member variable
    public isolated function getMember(string fieldName) returns MemberVariableStub {
        self.fieldName = fieldName;
        error? result = validateFieldNameExt(java:fromString(fieldName), self.mockObject);
        if (result is error) {
             panic result;
        }
        MemberVariableStub memberVariableStub = new MemberVariableStub(self.mockObject);
        memberVariableStub.fieldName = fieldName;
        return memberVariableStub;
    }
}

# Represents an object that allows stubbing member function invocations.
#
# + mockObject - Created mock object
# + functionName - Member function name
# + args - Arguments list of the function
# + returnValue - Value to return
# + returnValueSeq - Sequence of values to return
public class MemberFunctionStub {
    object {} mockObject;
    string functionName = "";
    (anydata|error)[] args = [];
    any|error returnValue = ();
    any|error returnValueSeq = [];

    # Gets invoked during the stub registration.
    #
    # + mockObject - Object to register
    public isolated function init(object{} mockObject) {
        self.mockObject = mockObject;
    }

    # Sets the arguments list to consider when stubbing the function call.
    #
    # + args - Arguments list
    # + return - Object that allows stubbing calls to provided member function
    public isolated function withArguments(anydata|error... args) returns MemberFunctionStub {
        self.args = args;
        error? result = validateArgumentsExt(self);
        if (result is error) {
            panic result;
        }
        return self;
    }

    # Sets the value to be returned when the function is called.
    #
    # + returnValue - Value or error to return
    public isolated function thenReturn(any|error returnValue) {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        self.returnValue = returnValue;
        error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is error) {
            panic thenReturnExtResult;
        }
    }

    # Sets the values to be returned when the function is called repeatedly.
    #
    # + returnValues - Value or error to return
    public isolated function thenReturnSequence(any|error... returnValues) {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        if (self.args.length() != 0) {
            error err = error("'withArguments' function cannot be specified with a return sequence");
            panic err;
        }
        self.returnValueSeq = returnValues;
        error? thenReturnSeqExtResult = thenReturnSeqExt(self);
        if (thenReturnSeqExtResult is error) {
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
        error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is error) {
            panic thenReturnExtResult;
        }
    }
}

# Represents an object that allows stubbing member variables retrieved.
#
# + mockObject - Created mock object
# + returnValue - Value to return
public class MemberVariableStub {
    object {} mockObject;
    any|error returnValue = ();
    string fieldName = "";

    # Gets invoked during the stub registration.
    #
    # + mockObject - Object to register
    public isolated function init(object{} mockObject) {
        self.mockObject = mockObject;
    }

    # Sets the value to be returned when the function is called.
    #
    # + returnValue - Value or error to return
    public isolated function thenReturn(any|error returnValue) {
        if (self.fieldName == "") {
             error err = error("field name is not specified.");
             panic err;
        }
        self.returnValue = returnValue;
        error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is error) {
            panic thenReturnExtResult;
        }
    }
}

# Objects and functions related to function mocking

# Allows a function to stub.
#
# + mockFunction - Function name to allow stubbing
# + return - Object that allows stubbing calls to provided function
public isolated function when(MockFunction mockFunction) returns FunctionStub {
    FunctionStub stub = new FunctionStub(mockFunction);
    return stub;
}

# Represents a MockFunction object.
public class MockFunction {
    string functionToMock = "";
    string functionToMockPackage = "";
    string[] mockFunctionClasses = [];
}

# Represents an object that allows stubbing function invocations.
#
# + mockFuncObj - Associated mockFunctionObj
# + returnValue - Return value
# + args - Function arguments
public class FunctionStub {
    MockFunction mockFuncObj;
    any|error returnValue = ();
    (anydata|error)[] args = [];


    # Gets invoked during the stub registration.
    #
    # + mockObject - Object to register
    public isolated function init(MockFunction mockFunction) {
        self.mockFuncObj = mockFunction;
    }

    # Sets the value to be returned when the function is called.
    #
    # + returnValue - Value or error to return
    public isolated function thenReturn(any|error returnValue) {
        self.returnValue = returnValue;
        error? result = thenReturnFuncExt(self);
        if (result is error) {
            panic result;
        }
    }

    # Sets the arguments list to consider when stubbing the function call.
    #
    # + args - Arguments list
    # + return - Object that allows stubbing calls to a function
    public isolated function withArguments(anydata|error... args) returns FunctionStub {
        self.args = args;
        return self;
    }

    # Sets the function behavior to do nothing when called.
    public isolated function doNothing() {
        error? result = thenReturnFuncExt(self);
        if (result is error) {
            panic result;
        }
    }

    # Sets a function to be invoked when the real function is called.
    #
    # + functionName - Mock function to call in place of the real
    public isolated function call(string functionName) {
        self.returnValue = "__CALL__" + functionName;
        error? result = thenReturnFuncExt(self);
        if (result is error) {
            panic result;
        }
    }

    # Sets the original function to be invoked.
    public isolated function callOriginal() {
        self.returnValue = "__ORIGINAL__";
        error? result = thenReturnFuncExt(self);
        if (result is error) {
            panic result;
        }
    }
}

# Creates a mock object of provided type description.
#
# + T - Type of object to create the mock
# + mockObject - Mock object to replace the original (optional)
# + return - Created mock object or throw an error if validation failed
public isolated function mock(typedesc<object{}> T, object{} mockObject = object { }) returns T = @java:Method {
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the mock object.
#
# + mockObject - Mock object
# + return - Return Value Description
isolated function validatePreparedObjExt(object{} mockObject) returns error? = @java:Method {
    name: "validatePreparedObj",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the provided function name.
#
# + functionName - Function name provided
# + mockObject - Object to validate against
# + return - error if function does not exist or in case of a signature mismatch
isolated function validateFunctionNameExt(handle functionName, object{} mockObject) returns error? = @java:Method {
    name: "validateFunctionName",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the field name.
#
# + fieldName - Field name provided
# + mockObject - Object to validate against
# + return - error if field does not exist
isolated function validateFieldNameExt(handle fieldName, object{} mockObject) returns error? = @java:Method {
    name: "validateFieldName",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to validate the arguments list.
#
# + case - Case to validate
# + return - error in case of an argument mismatch
isolated function validateArgumentsExt(MemberFunctionStub case) returns error? = @java:Method {
    name: "validateArguments",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to register the return value.
#
# + case - Case to register
# + return - error if the case registration fails
isolated function thenReturnExt(MemberFunctionStub|MemberVariableStub case) returns error? = @java:Method {
    name: "thenReturn",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to register the sequence of return values.
#
# + case - Case to register
# + return - error if the case registration fails
isolated function thenReturnSeqExt(MemberFunctionStub case) returns error? = @java:Method {
    name: "thenReturnSequence",
    'class: "org.ballerinalang.testerina.natives.mock.ObjectMock"
} external;

# Inter-op to register return value.
#
# + case - Case to register
# + return - error if the case registration fails
isolated function thenReturnFuncExt(FunctionStub case) returns error? = @java:Method {
    name: "thenReturn",
    'class: "org.ballerinalang.testerina.natives.mock.FunctionMock"
} external;

# Inter-op to handle function mocking.
#
# + mockFunction - The `mockFunction` object
# + args - Function arguments
# + return - Function return value or an error if the case registration fails
isolated function mockHandler(MockFunction mockFunction, (any|error)... args) returns any|error = @java:Method {
    name: "mockHandler",
    'class: "org.ballerinalang.testerina.natives.mock.FunctionMock"
} external;
