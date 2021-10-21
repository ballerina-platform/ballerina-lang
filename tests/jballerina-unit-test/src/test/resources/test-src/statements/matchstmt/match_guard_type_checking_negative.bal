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


isolated function fn() {
}

function f1() {
    map<int> i = {a: 1};

    match i {
        {b: 2, c: 3} if fn() => {
        }
    }
}

function f2() {
    int i = 1;
    int j = 1;

    match i {
        0 => {
        }
        1 if 1 + 2 => {
        }
        2 if (j == 1) || (1 + 2) || (j != 4) => {
        }
        3 if j => {
        }
    }
}
