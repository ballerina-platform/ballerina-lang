// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

public function calculate(int a, int b, string operation) returns int {
    int result = 0;
    if (operation == "add") {
        result = a + b;
    } else if (operation == "sub") {
        result = a - b;
    } else if (operation == "mul") {
        result = a * b;
    } else if (operation == "div") {
        result = a / b;
    }
    return result;
}
