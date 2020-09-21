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

public function main() {
}

//
// FUNCTIONS
//

// Returns (int)
public function intAdd(int a, int b) returns (int) {
    return a + b;
}

// Returns (string)
public function stringAdd(string str) returns (string) {
    return "test_" + str;
}

// Returns (float)
public function floatAdd(float c, float d) returns (float) {
    return c + d;
}

// Call mocked function
public function callIntAdd(int x, int y) returns (int) {
    return intAdd(x, y);
}

// Function with varargs
public function intAdd3((any|error)... intValues) returns (int) {
    int sum = 0;

    foreach var arg in intValues {
        if (arg is int) {
            sum += arg;
        }
    }

    return  sum;
}

// Class calling mocked function
public class TestClass {
    function init() {}

    function add(int a, int b) returns (int) {
        return intAdd(a, b);
    }
}
