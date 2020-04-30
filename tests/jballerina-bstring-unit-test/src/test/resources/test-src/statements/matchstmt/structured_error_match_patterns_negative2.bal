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

const constReason = "reason";
type ErrorData record {
    string a?;
    error err?;
    map<string> m?;
    string message?;
    error cause?;
};
type ER error<string, ErrorData>;

function testIndirectErrorMatchPattern() returns string {
    ER err1 = error("Error Code", message = "Msg");
    match err1 {
        ER ( message = m, ...var rest) => {
            return <string>m;
        }
        error(constReason, ...var rest) => {
            return constReason;
        }
        error(reason, ...var rest) => {
            return constReason;
        }
    }
    return "Default";
}

function noVarReasonErrorMatch(any|error a) returns string {
    match a {
        error(r) => { return <string> r;} // should be error(var r);
		12 => { return "matched 12";}
    }

    return "default";
}

type ER2 error<string, ErrorDataABC>; // ErrorData undefined

function testIndirectErrorMatchPattern1() returns string {
    ER2 err =  error ("Error Code" , message  =  "Msg");
    match err {
        ER2  (message = m, ... var rest) => { return <string>m; }
    }
    return "Default";
}
