// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Error error<record {|int code; string...;|}>;

function test() {
    string msg;
    error cause;
    int code;
    map<string> rbp5;
    error Error(msg, cause, code=code, ...rbp5) = error Error("FileNotFound", code = 400); // destructuring assignment stmt
    error (msg, cause, code=code, ...rbp5) = error Error("FileNotFound", code = 400);
    Error error Error(msg1, cause1, code=code1, ...rbp6) = error Error("FileNotFound", code = 500); // local var decl stmt
    Error error (msg2, cause2, code=code2, ...rbp7) = error Error("FileNotFound", code = 500);
}
