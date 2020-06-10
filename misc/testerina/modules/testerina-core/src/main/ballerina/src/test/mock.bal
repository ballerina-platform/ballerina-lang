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
public const INVALID_OBJECT_ERROR = "{ballerina/test}InvalidObjectError";
public type InvalidObjError error<INVALID_OBJECT_ERROR, Detail>;

# Represents the reason for the non-existing member function related errors.
public const FUNCTION_NOT_FOUND_ERROR = "{ballerina/test}FunctionNotFoundError";
public type FunctionNotFoundError error<FUNCTION_NOT_FOUND_ERROR, Detail>;

# Represents the reason for the function signature related errors.
public const FUNCTION_SIGNATURE_MISMATCH_ERROR = "{ballerina/test}FunctionSignatureMismatchError";
public type FunctionSignatureMismatchError error<FUNCTION_SIGNATURE_MISMATCH_ERROR, Detail>;

# Represents the reason for the object member field related errors.
public const INVALID_MEMBER_FIELD_ERROR = "{ballerina/test}InvalidMemberFieldError";
public type InvalidMemberFieldError error<INVALID_MEMBER_FIELD_ERROR, Detail>;

public type Error InvalidObjError | FunctionNotFoundError | FunctionSignatureMismatchError | InvalidMemberFieldError;

# The details of an error.
#
# + message - Specific error message of the error
# + cause - Any other error, which causes this error
public type Detail record {
    string message;
    error cause?;
};

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

# Prepares a provided mock object.
#
# + mockObj - created mock object
# + return - prepared object that expects a member functon/field to mock
public function prepare(object {} mockObj) returns MockObj {
    Error? result = validatePrepareObjExt(mockObj);
    if (result is Error) {
        panic result;
    }
    MockObj obj = new MockObj(mockObj);
    return obj;
}

# Initial mock object created to expose functions to user to regster cases
public type MockObj object {
    object {} prepareObj;
    string functionName = "";
    string fieldName = "";

    # Gets invoked during the mock object preparation.
    #
    # + prepareObj - object to register cases
    # + return - mock object
    public function init(object{} prepareObj) {
        self.prepareObj = prepareObj;
    }

    # Accepts a member function to mock.
    #
    # + funcName - member funcion name
    # + return - mock case that expects the function behavior
    public function when(string funcName) returns Case {
        self.functionName = funcName;
        Error? result = validateFunctionNameExt(self);
        if (result is Error) {
             panic result;
        }
        Case mockObjCase = new Case(self.prepareObj);
        mockObjCase.functionName = funcName;
        return mockObjCase;
    }

    # Accepts a member field to mock
    #
    # + fieldName - memeber field name
    # + return - mock case that expects the value to return
    public function getMember(string fieldName) returns Case {
        self.fieldName = fieldName;
        Error? result = validateFieldNameExt(self);
        if (result is Error) {
             panic result;
        }
        Case mockObjCase = new Case(self.prepareObj);
        mockObjCase.fieldName = fieldName;
        return mockObjCase;
    }
};

# Represents a single case of a mock object
#
# + prepareObj - created mock object
# + functionName - member function name
# + args - arguments list of the function
# + returnVal - value to return
# + returnValSeq - equence of values to return
# + fieldName - field name
public  type Case object {
    object {} prepareObj;
    string functionName = "";
    anydata|error args = [];
    any|error returnVal = ();
    any|error returnValSeq = [];
    string fieldName = "";

    # Gets invoked during the mock case registration.
    #
    # + prepareObj - object to register cases
    # + return - mock object
    public function init(object{} prepareObj) {
        self.prepareObj = prepareObj;
    }

    # Accepts the arguments list to pass to the member function.
    #
    # + args - arguments list
    # + return - mock case that expects the function behavior
    public function withArguments(anydata|error... args) returns Case {
        if (self.functionName == "") {
             error err = error("function to mock is not specified.");
             panic err;
        }
        self.args = args;
        return self;
    }

    # Sets the value to be returned when the function is called.
    #
    # + retVal - return value
    public function thenReturn(any|error retVal) {
        if (self.functionName == "" && self.fieldName == "") {
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
        if (self.functionName == "" && self.fieldName == "") {
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

// Interop functions

# Inter-op to create the mock object
#
# + T - type description
# + obj - mock object
# + return - type casted mock object or and error if creation failed
function mockExt(typedesc<object {}> T, object {} obj) returns object{}|Error = @java:Method {
    name: "mock",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to register the return value
#
# + case - case to register
# + return - error if case registration failed
function thenReturnExt(object{} case) returns Error? = @java:Method {
    name: "thenReturn",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to register the sequence of return values;
#
# + case - case to register
# + return - error if case registration failed
function thenReturnSeqExt(object{} case) returns Error? = @java:Method {
    name: "thenReturnSequence",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the mock object.
#
# + prepareObj - mock object
# + return - Return Value Description
function validatePrepareObjExt(object{} prepareObj) returns Error? = @java:Method {
    name: "validatePrepareObj",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the provided function name
#
# + case - case to validate
# + return - error if function does not exist or in case of a signature mismatch
function validateFunctionNameExt(object{} case) returns Error? = @java:Method {
    name: "validateFunctionName",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;

# Inter-op to validate the field name.
#
# + case - case to validate
# + return - error if field does not exist
function validateFieldNameExt(object{} case) returns Error? = @java:Method {
    name: "validateFieldName",
    class: "org.ballerinalang.testerina.natives.test.Mock"
} external;
