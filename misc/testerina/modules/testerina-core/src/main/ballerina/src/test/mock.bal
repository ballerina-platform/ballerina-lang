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

# Object mocking constants

public const ANY = "__ANY__";

# Object mocking errors

# Represents the reason for the mock object related errors.
public const INVALID_OBJECT_ERROR = "InvalidObjectError";
public type InvalidObjError distinct error<Detail>;

# Represents the reason for the non-existing member function related errors.
public const FUNCTION_NOT_FOUND_ERROR = "FunctionNotFoundError";
public type FunctionNotFoundError distinct error<Detail>;

# Represents the reason for the function signature related errors.
public const FUNCTION_SIGNATURE_MISMATCH_ERROR = "FunctionSignatureMismatchError";
public type FunctionSignatureMismatchError distinct error<Detail>;

# Represents the reason for the object member field related errors.
public const INVALID_MEMBER_FIELD_ERROR = "InvalidMemberFieldError";
public type InvalidMemberFieldError distinct error<Detail>;

# Represents the reason for function mocking related errors.
public const FUNCTION_CALL_ERROR = "FunctionCallError";
public type FunctionCallError distinct error<Detail>;

public type Error InvalidObjError | FunctionNotFoundError | FunctionSignatureMismatchError | InvalidMemberFieldError | FunctionCallError;

# The details of an error.
public type Detail record {};

# Objects and functions related to exposed API

# Creates and returns a mock object of provided type description
#
# + T - Type Description (typedesc)
# + mockObj - mock object to replace the original (optional)
# + return - created mock object
public function mock(typedesc<object {}> T, object{} mockObj = new) returns object{} {
    object {}|Error mockExtResult = mockExt(T, mockObj);
    if (mockExtResult is Error) {
        panic mockExtResult;
    }
    return <object{}>mockExtResult;
}

# Prepares a provided mock object to register mock cases.
#
# + mockObj - created mock object
# + return - prepared object that expects a member functon/field to mock
public function prepare(object {} mockObj) returns MockObj {
    Error? result = validatePreparedObjExt(mockObj);
    if (result is Error) {
        panic result;
    }
    MockObj obj = new MockObj(mockObj);
    return obj;
}

# Initial mock object created to expose functions to user to regster cases
public type MockObj object {
    object {} preparedObj;
    string fieldName = "";

    # Gets invoked during the mock object preparation.
    #
    # + preparedObj - object to register cases
    # + return - mock object
    public function init(object{} preparedObj) {
        self.preparedObj = preparedObj;
    }

    # Accepts a member function to mock.
    #
    # + funcName - member funcion name
    # + return - mock case that expects the function behavior
    public function when(string funcName) returns CaseMemFunc {
        Error? result = validateFunctionNameExt(java:fromString(funcName), self.preparedObj);
        if (result is Error) {
             panic result;
        }
        CaseMemFunc mockObjCaseMemFunc = new CaseMemFunc(self.preparedObj);
        mockObjCaseMemFunc.functionName = funcName;
        return mockObjCaseMemFunc;
    }

    # Accepts a member field to mock
    #
    # + fieldName - memeber field name
    # + return - mock case that expects the value to return
    public function getMember(string fieldName) returns CaseMemVar {
        self.fieldName = fieldName;
        Error? result = validateFieldNameExt(java:fromString(fieldName), self.preparedObj);
        if (result is Error) {
             panic result;
        }
        CaseMemVar mockObjCaseMemVar = new CaseMemVar(self.preparedObj);
        mockObjCaseMemVar.fieldName = fieldName;
        return mockObjCaseMemVar;
    }
};

# Represents a single case of a member function
#
# + preparedObj - created mock object
# + functionName - member function name
# + args - arguments list of the function
# + returnVal - value to return
# + returnValSeq - equence of values to return
public  type CaseMemFunc object {
    object {} preparedObj;
    string functionName = "";
    anydata|error args = [];
    any|error returnVal = ();
    any|error returnValSeq = [];

    # Gets invoked during the mock case registration.
    #
    # + preparedObj - object to register cases
    # + return - mock object
    public function init(object{} preparedObj) {
        self.preparedObj = preparedObj;
    }

    # Accepts the arguments list to pass to the member function.
    #
    # + args - arguments list
    # + return - mock case that expects the function behavior
    public function withArguments(anydata|error... args) returns CaseMemFunc {
        self.args = args;
        Error? result = validateArgumentsExt(self);
        if (result is Error) {
            panic result;
        }
        return self;
    }

    # Sets the value to be returned when the function is called.
    #
    # + retVal - return value
    public function thenReturn(any|error retVal) {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        self.returnVal = retVal;
        Error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is Error) {
            panic thenReturnExtResult;
        }
    }

    # Sets the values to be returned when the function is called repeatedly.
    #
    # + retVals - return values
    public function thenReturnSequence(any|error... retVals) {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        if (self.args != []) {
            error err = error("'withArguments' function cannot be specified with a return sequence");
            panic err;
        }
        self.returnValSeq = retVals;
        Error? thenReturnSeqExtResult = thenReturnSeqExt(self);
        if (thenReturnSeqExtResult is Error) {
            panic thenReturnSeqExtResult;
        }
    }

    # Sets the function behavior to do nothing when called.
    public function doNothing() {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        self.returnVal = ();
        Error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is Error) {
            panic thenReturnExtResult;
        }
    }
};

# Represents a single case of a memeber variable
#
# + preparedObj - created mock object
# + returnVal - value to return
public  type CaseMemVar object {
    object {} preparedObj;
    any|error returnVal = ();
    string fieldName = "";

    # Gets invoked during the mock case registration
    #
    # + preparedObj - object to register cases
    # + return - mock object
    public function init(object{} preparedObj) {
        self.preparedObj = preparedObj;
    }

    # Sets the value to be returned when the function is called.
    #
    # + retVal - return value
    public function thenReturn(any|error retVal) {
        if (self.fieldName == "") {
             error err = error("field name is not specified.");
             panic err;
        }
        self.returnVal = retVal;
        Error? thenReturnExtResult = thenReturnExt(self);
        if (thenReturnExtResult is Error) {
            panic thenReturnExtResult;
        }
    }
};

// Inter-op functions
public function when(MockFunction mockFunc) returns CaseFunction {
    CaseFunction case = new CaseFunction(mockFunc);
    return case;
}

# Represents a MockFunction object
public type MockFunction object {};

# Represents a CaseFunction object
#
# + mockFuncObj - associated mockFunctionObj
# + returnVal - return value
# + args - function arguments
public type CaseFunction object {
    MockFunction mockFuncObj;
    any|error returnVal = ();
    anydata|error args = [];

    public function init(MockFunction mockFunc) {
        self.mockFuncObj = mockFunc;
    }

    public function thenReturn(any|error retVal) {
        self.returnVal = retVal;
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }

    public function withArguments(anydata|error... args) returns CaseFunction {
        self.args = args;
        return self;
    }

    public function doNothing() {
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }

    public function call(string functionName) {
        self.returnVal = "__CALL__" + functionName;
        Error? result = thenReturnFuncExt(self);
        if (result is Error) {
            panic result;
        }
    }
};

# Inter-op to create the mock object
#
# + T - type description
# + obj - mock object
# + return - type casted mock object or and error if creation failed
function mockExt(typedesc<object {}> T, object {} obj) returns object{}|Error = @java:Method {
    name: "mock",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the mock object.
#
# + preparedObj - mock object
# + return - Return Value Description
function validatePreparedObjExt(object{} preparedObj) returns Error? = @java:Method {
    name: "validatePreparedObj",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the provided function name
#
# + functionName - function name provided
# + preparedObj - object to validate against
# + return - error if function does not exist or in case of a signature mismatch
function validateFunctionNameExt(handle functionName, object{} preparedObj) returns Error? = @java:Method {
    name: "validateFunctionName",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the field name.
#
# + fieldName - field name provided
# + preparedObj - obj to validate against
# + return - error if field does not exist
function validateFieldNameExt(handle fieldName, object{} preparedObj) returns Error? = @java:Method {
    name: "validateFieldName",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the arguments list.
#
# + case - case to validate
# + return - error in case of an argument mismatch
function validateArgumentsExt(CaseMemFunc case) returns Error? = @java:Method {
    name: "validateArguments",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to register the return value
#
# + case - case to register
# + return - error if case registration failed
function thenReturnExt(CaseMemFunc|CaseMemVar case) returns Error? = @java:Method {
    name: "thenReturn",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to register the sequence of return values;
#
# + case - case to register
# + return - error if case registration failed
function thenReturnSeqExt(CaseMemFunc case) returns Error? = @java:Method {
    name: "thenReturnSequence",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to register return value
#
# + case - case to register
# + return - error if case registration failed
function thenReturnFuncExt(CaseFunction case) returns Error? = @java:Method {
    name: "thenReturn",
    class: "org.ballerinalang.testerina.natives.test.FunctionMock"
} external;

# Inter-op to call Mock Handler
#
# + mockFunction - mockFunction object
# + args - function arguments
# + return - function return value or error if case registration failed
public function mockHandler(MockFunction mockFunction, anydata|error[] args) returns any|Error = @java:Method {
    name: "mockHandler",
    class: "org.ballerinalang.testerina.natives.test.FunctionMock"
} external;

