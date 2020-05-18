// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function tupleDestructureTest1() returns [int, string, string] {
	[int, string...] x = [1, "a", "b"];
    int a;
    string b;
    string c;
    [a, b, c] = x;
    return [a, b, c];
}

function tupleDestructureTest2() returns [int, int[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    int[] b;
    [a, ...b] = x;
    return [a, b];
}

function tupleDestructureTest3() returns [int, string[]] {
    [int, string...] x = [1, "a", "b"];
    int a;
    string[] b;
    [a, b] = x;
    return [a, b];
}

function tupleDestructureTest4() returns [int, int] {
    [int, int, int] x = [1, 2, 3];
    int a;
    int b;
    [a, b] = x;
    return [a, b];
}
