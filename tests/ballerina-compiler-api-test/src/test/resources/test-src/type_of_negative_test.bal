// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function mainTest() {
    Application ap = {};
    testFuncCall(a = 1, application = {});
}

type CustomError error<ErrorData>;

public function checkError() returns CustomError? {
    return error CustomError("Custom error", errorCode = "123", application = {});
}

// utils
type Application record {|
    string id;
    string name;
    int 'version;
|};

type ErrorData record {
    string errorCode;
    Application application;
};

function testFuncCall(int a, Application application) {
}
