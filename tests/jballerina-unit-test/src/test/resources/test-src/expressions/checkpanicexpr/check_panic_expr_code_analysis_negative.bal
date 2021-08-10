// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testCheckedExprErrors1() returns error? {
    string line = checkpanic readLineError();
    return;
}

function readLineError() returns error {
    error e = error("io error");
    return e;
}

function testCheckedExprErrors2() returns error? {
    string line = checkpanic readLine();
    return;
}

public type MyError error<record { int code; string message?; error cause?; }>;

public type CustomError error<record { int code; string data; string message?; error cause?; }>;

function readLine() returns MyError | CustomError {
    MyError e = error MyError("io error", code = 0);
    return e;
}
