// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.com).
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

int a = check natural (new MyGenerator()) {
    What is 1 + 1?
};

function f1() {
    int[] _ = const natural {
        Give me an array of length 10 with random values between 1 and 100.
    };
}

function f2(int a, int b) returns int|error = @natural:code {
    prompt: "What's the sum of these values?"
} external;

isolated client class MyGenerator {
    remote isolated function generate(
            natural:Prompt prompt, typedesc<anydata> td) returns td|error = external;
}
