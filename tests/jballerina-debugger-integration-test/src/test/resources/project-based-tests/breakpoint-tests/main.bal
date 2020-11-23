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

# Contains an if condition to test the code coverage
public function main() {
    int a = 10;
    int x = 0;

    if (a == 10) {
        x = 1;
    } else {
        x = 2;
    }
   
    function1();
    function2();

    x = 3;
    x = 4;
    x = 5;
}

public function function1() {
    int y = 0;
}

public function intAdd(int a, int b) returns int {
    return (a + b);
}
