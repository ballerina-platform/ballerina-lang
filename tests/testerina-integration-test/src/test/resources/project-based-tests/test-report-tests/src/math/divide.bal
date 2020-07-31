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

# Divides two integers.
#
#
# + return - result after division or error if divisor is zero
public function divideInt() returns (int|error) {
    int a = 10;
    int b = 0;
    int|error result;
    if (b == 0) {
        result = error("division by zero");
    } else {
        result = a/b;
    }
    return result;
}
