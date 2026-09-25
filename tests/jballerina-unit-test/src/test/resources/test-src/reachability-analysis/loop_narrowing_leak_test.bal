// Copyright (c) 2026 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testForeachBodyExitEnvNotLeaked(int|string|boolean x, int[] arr) returns string|boolean {
    if x is int|string {
        foreach int i in arr {
            if x is int {
                return "was int";
            }
        }
    }
    // ERROR: a foreach loop can run zero times, so x is not narrowed here - it could still be int
    string|boolean s = x;
    return s;
}

function testWhileBodyExitEnvNotLeaked(int|string|boolean x, boolean cond) returns string|boolean {
    if x is int|string {
        while cond {
            if x is int {
                return "was int";
            }
        }
    }
    // ERROR: a while loop can run zero times, so x is not narrowed here - it could still be int
    string|boolean s = x;
    return s;
}
