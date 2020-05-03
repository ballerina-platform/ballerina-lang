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

const ERR_REASON = "const error reason";

type ErrorRecord record {
    string message;
    int statusCode;
    error cause?;
};

type USER_DEF_ERROR error<ERR_REASON, ErrorRecord>;

public function main(string s, int code) returns error? {
    io:print("error? returning main invoked");
    match s {
        "error" => {
            error e = error("generic error", statusCode = code);
            return e;
        }
        "nil" => {
            return;
        }
        "user_def_error" => {
            USER_DEF_ERROR e = error(ERR_REASON, message = "error message", statusCode = code);
            return e;
        }
    }
    panic error("expected to have returned within match");
}
