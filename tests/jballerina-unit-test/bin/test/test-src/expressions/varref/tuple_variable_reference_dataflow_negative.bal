// Copyright (c) 2020 WSO2 Inc. [http://www.wso2.org] All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 [the "License"]; you may not use this file except
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

function testAssigningValuesToFinalVars() {
    [string, float] t1 = ["hello", 1.0];
    final var [s1, f1] = t1;
    [s1, f1] = t1;

    [string?, [float, boolean], int...] t2 = ["hi", [1.0, true], 1, 2];
    final [string?, [float, boolean], int...] [s2, [f2, b2], ...n2] = t2;
    [s2, [f2, b2], ...n2] = t2;
    s2 = "hello";
    [f2, b2] = [2.0, false];
}
