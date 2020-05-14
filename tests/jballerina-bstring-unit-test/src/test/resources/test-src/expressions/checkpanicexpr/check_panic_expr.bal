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

function testBasicCheckpanic(int testNumber) returns int|float {
    match testNumber {
        1 => {int i = checkpanic getGenericError();}
        2 => {int i = checkpanic getGenericErrorWithDetail();}
        3 => {int|boolean i = checkpanic getGenericErrorWithMultiUnion();}
        4 => {return checkpanic getFloat();}
        5 => {int i = checkpanic returnBallerinaPanicedError();}
        6 => {int i = checkpanic getCustomError();}
    }
    return 0.0;
}

function getGenericError() returns int|error {
    error e = error("Generic Error");
    return e;
}

function getGenericErrorWithDetail() returns int|error {
    error e = error("Generic Error", fatal = true, message = "Something Went Wrong");
    return e;
}

function getGenericErrorWithMultiUnion() returns int|boolean|error {
    error e = error("Generic Error");
    return e;
}

function getFloat() returns int|float|error {
    float f = 2.2;
    return f;
}

function returnBallerinaPanicedError() returns int|error {
    int[2] arr = [1, 2];
    int[] oArr = arr;
    int|error ret = trap oArr[4];
    return ret;
}

public type MyError error<string, record { int code; string message?; error cause?;}>;

function getCustomError() returns int|MyError {
    MyError e = error("My Error", code = 12);
    return e;
}
