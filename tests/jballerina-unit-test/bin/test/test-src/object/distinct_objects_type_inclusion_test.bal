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

public type Status distinct object {
    public int code;
};

public type StatusCodeResponse record {|
    Status status;
|};

public type InternalServerErrorStatus object {
    *Status;
    public byte code;
};

type InternalServerErrorStatusImmut readonly & object {
    *Status;
    public byte code;
};

public type InternalServerError record {|
    *StatusCodeResponse;
    InternalServerErrorStatus status = object {
        public byte code = 101;
    };
|};

public function testDistinctObjectSubtyping() {
    InternalServerErrorStatus x = object {
        public byte code = 101;
    };
    Status y = x;

    InternalServerError k = {};

    if !(y is InternalServerErrorStatus) {
        panic error(
           string `Expected a value of type: ${InternalServerErrorStatus.toString()}, found: ${(typeof y).toString()}`);
    }

    if k.status.code != 101 {
        panic error("Expected: 101, found: " + k.status.code.toString());
    }

    y = object InternalServerErrorStatusImmut {
        public byte code = 102;
    };

    if y.code != 102 {
        panic error("Expected: 102, found: " + y.code.toString());
    }
}
