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
import ballerina/io;

function testArrayWithErrors() returns [string, string, string] {
    error<string, map<string|boolean>> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, map<string|boolean>> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, map<string|boolean>> err3 = error("Error Three", message = "msgThree", fatal = true);
    error<string, map<string|boolean>>[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason, message = message, fatal = fatal) in errorArray {
        result1 += reason + ":";
        result1 += io:sprintf("%s:", message);
        result1 += io:sprintf("%s:", fatal);
    }

    string result2 = "";
    foreach error<string, map<string|boolean>> error(reason2, message = message1, fatal = fatal1) in errorArray {
        result2 += reason2 + ":";
        any temp2 = message;
        result2 += io:sprintf("%s:", message1);
        result2 += io:sprintf("%s:", fatal1);
    }

    string result3 = "";
    foreach var error(reason3) in errorArray {
        result3 += reason3 + ":";
    }
    foreach error<string, map<string|boolean>> error(reason4, ..._) in errorArray {
        result3 += reason4 + ":";
    }
    return [result1, result2, result3];
}


function testMapWithErrors() returns [string, string, string] {
    error<string, map<string|boolean>> err1 = error("Error One", message = "msgOne", fatal = true);
    error<string, map<string|boolean>> err2 = error("Error Two", message = "msgTwo", fatal = false);
    error<string, map<string|boolean>> err3 = error("Error Three", message = "msgThree", fatal = true);
    map<error<string, map<string|boolean>>> errMap = { a: err1, b: err2, c: err3 };

    string result1 = "";
    foreach var [key, error(reason, message = message, fatal = fatal)] in errMap {
        result1 += reason + ":";
        result1 += io:sprintf("%s:", message);
        result1 += io:sprintf("%s:", fatal);
    }

    string result2 = "";
    foreach [string, error<string, map<string|boolean>>] [key, error(reason2, message = message, fatal = fatal)] in errMap {
        result2 += reason2 + ":";
        any temp2 = message;
        if (temp2 is string|boolean|()) {
            result2 += io:sprintf("%s:", message);
            result2 += io:sprintf("%s:", fatal);
        }
    }

    string result3 = "";
    foreach var [key, error(reason3)] in errMap {
        result3 += reason3 + ":";
    }
    foreach [string, error<string, map<string|boolean>>] [key, error(reason4, ..._)] in errMap {
        result3 += reason4 + ":";
    }
    return [result1, result2, result3];
}
