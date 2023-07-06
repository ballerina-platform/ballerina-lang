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

type first record {
    int|error f0 = 1;
    int|error f00 = 2;
};

public type secondRec record {
    first|error f1;
    first|error f2;
};

public function testStrand() returns secondRec {
    future<int|error> f0 = start add(1, 2);
    future<int|error> f00 = start add(1, 2);
    future<first> f1 = @strand{thread:"any"} start concat(f0, f00);
    future<first> f2 = @strand{thread:"any"} start concat(f0, f00);
    secondRec result = wait {f1, f2};
    return result;
}

function add(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 9999999) {
        l = l + 1;
    }
    return k;
}

function concat(future<int|error> f0, future<int|error> f00) returns first {
    first result = wait {f0, f00};
    return result;
}


