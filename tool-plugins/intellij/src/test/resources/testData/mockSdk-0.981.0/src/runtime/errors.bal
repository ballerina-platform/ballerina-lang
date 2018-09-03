// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

documentation {
    Representation of `NullReferenceException`

    F{{message}} error message
    F{{cause}} optional error cause
}
public type NullReferenceException record {
    string message;
    error? cause;
};

documentation {
    Representation of `IllegalStateException`

    F{{message}} error message
    F{{cause}} optional error cause
}
public type IllegalStateException record {
    string message;
    error? cause;
};

documentation {
    Representation of `CallStackElement`

    F{{callableName}} Callable name
    F{{packageName}} Package name
    F{{fileName}} File name
    F{{lineNumber}} Line number
}
public type CallStackElement record {
    string callableName;
    string packageName;
    string fileName;
    int lineNumber;
};

documentation {
    Retrieves the Call Stack

    R{{}} Array of `CallStackElement` elements
}
public extern function getCallStack() returns (CallStackElement[]);

documentation {
    Retrieves the Call Stack Frame for a particular error

    P{{e}} optional `error` instance
    R{{}} `CallStackElement` instance
}
public extern function getErrorCallStackFrame(error? e) returns (CallStackElement);

documentation {
    Representation of `CallFailedException`

    F{{message}} Error message
    F{{cause}} optional `error` instance
    F{{causes}} optional array of `error` instances
}
public type CallFailedException record {
    string message;
    error? cause;
    error[]? causes;
};
