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

import ballerina/io;

function testVariableScope() returns error?{
        do {
            io:println("This is inside do");
            int i = 0;
            fail getError();
        } on fail error e {
            io:println("This is inner on-fail" + i.toString());
        }
    io:println("While is completed.");
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}
