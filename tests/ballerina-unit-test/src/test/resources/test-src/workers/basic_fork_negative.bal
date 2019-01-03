// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function forkToDefaultWorkerInteraction() returns int {
    int x = 5;
    fork {
        worker w1 {
            int a = 5;
            int b = 0;
            a -> w2;
            b = <- w2;
            a -> default;
        }
        worker w2 {
            int a = 0;
            int b = 15;
            a = <- w1;
            b -> w1;
            b -> default;
        }
    }
    map<any> results = wait {w1, w2};
    io:println(results);
    return x;
}
