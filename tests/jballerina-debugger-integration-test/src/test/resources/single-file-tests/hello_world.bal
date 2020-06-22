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

// The `main` function, which acts as the entry point.
public function main() {
    int a = 10;
    int b = 5;
    int c = a + b;

    if (c == 15) {
        io:println("c is equal to 15");
    } else {
        io:println("c is not equal to 15");
    }

    sayHello();
}

function sayHello() {
    io:println("Hello, World!");
}
