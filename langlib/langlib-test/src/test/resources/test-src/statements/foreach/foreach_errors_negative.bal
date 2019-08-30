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

import ballerina/lang.'error as lang;

type Detail record {
    *lang:Detail;
    boolean fatal;
};

function testArrayWithErrors1() returns string {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    error<string, Detail>?[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason, message = message, fatal = fatal) in errorArray {
    // invalid error variable; expecting an error type but found 'error?' in type definition

    }
    return result1;
}

function testArrayWithErrors2() returns string {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    error<string, Detail>[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach error<string, Detail> error(reason, message = message, fatal = fatal) in errorArray {
    // incompatible types: expected 'error', found 'error'

    }
    return result1;
}

function testArrayWithErrors3() returns string {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    error<string, Detail>[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason,  message = message, fatal = fatal) in errorArray {
        map<string> x = message; // incompatible types: expected 'map<string>', found 'string'
    }

    foreach var error(reason1,  message = message1, fatal = fatal1) in errorArray {
        string|boolean x = message1; // incompatible types: expected 'string|boolean', found 'string|boolean?'
    }
    return result1;
}

function testMapWithErrors1() returns string {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    map<error<string, Detail>?> errMap = { a: err1, b: err2, c: err3 };

    string result1 = "";
    foreach var error(reason,  message = message2, fatal = fatal2) in errMap {
    // invalid error variable; expecting an error type but found 'error?)' in type definition

    }

    return result1;
}

function testMapWithErrors2() returns string {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    map<error<string, Detail>> errMap = { a: err1, b: err2, c: err3 };


    string result1 = "";
    foreach error<string, Detail> error(reason,  message = message, fatal = fatal) in errMap {
    // incompatible types: expected '(string,error)', found '(string,error)'

    }
    return result1;
}

function testMapWithErrors3() returns string {
    error<string, Detail> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, Detail> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, Detail> err3 = error("Error Three", message = "msgThree", fatal = true);
    map<error<string, Detail>> errMap = { a: err1, b: err2, c: err3 };

    string result1 = "";
    foreach var error(reason, message = message3, fatal = fatal3) in errMap {
        map<string> x = message3;
    }

    foreach var error(reason1, message = message4) in errMap {
        string|boolean x = message4;
    }
    return result1;
}

type ReasonT record {|
    string message;
    boolean fatal;
    error cause?;
|};

function testForeachReasonBinding() {
    error<string, ReasonT> err0 = error("reason-0", message = "message", fatal = true);
    error<string, ReasonT> err1 = error("reason-1", message = "message", fatal = false);
    map<error<string, ReasonT>> errMap = {a: err0, b: err1};

    foreach var error(reason, message = message5, fatal = fatal5, other2 = otherVar) in errMap {
        string m = message5;
        boolean f = fatal5;
        int i = otherVar;
    }
}
