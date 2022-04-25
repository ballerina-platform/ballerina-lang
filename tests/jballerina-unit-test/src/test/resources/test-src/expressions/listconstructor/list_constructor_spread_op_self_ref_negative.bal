// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testSelfReferenceError() {
    int[] a1 = [...a1];
    int[] a2 = [1, ...a2, 2];
    int[1] a3 = [...a3];
    [int, string] a4 = [3, ...a4];
    [int, string, any...] a5 = [3, ...a5];
    int[3] a6 = [3, ...[...[...a6]]];
}
