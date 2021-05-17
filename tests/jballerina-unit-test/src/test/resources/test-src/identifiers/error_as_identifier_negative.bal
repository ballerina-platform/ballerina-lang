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

class ErrorField {
    public error 'error;
    public error 'error;
    public int 'int;

    function init(error er, int value = 20) {
        self.'error = er;
        self.'int = value;
    }
}

type ErrorDataWithErrorField record {
    error 'error;
    error 'error;
};

function getError() returns error {
    return error("Generated Error");
}

function funcErrorNamedDefaultArgument(error 'error = getError(), error 'error = getError()) {}

function funcErrorNamedIncludedParam(*ErrorDataWithErrorField 'error, *ErrorDataWithErrorField 'error) {}

function funcErrorNamedRequiredParam(ErrorDataWithErrorField 'error, ErrorDataWithErrorField 'error) {}

function funcErrorNamedRestParam(ErrorDataWithErrorField 'error, ErrorDataWithErrorField... 'error) {}

function funcErrorNamedIncludedAndRequiredParam(ErrorDataWithErrorField 'error, *ErrorDataWithErrorField 'error) {}

