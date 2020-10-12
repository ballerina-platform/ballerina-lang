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

const constLength = 2;
int intLength = 2;
const stringLength = "2";

function arraySizeTest() {
    int[constLength] a = [1, 2, 3];
    int[intLength] b = [1, 2];
    int[stringLength] c = [1, 2];
    int[length] d = [1, 2];
    int[2] e = [1, 2, 3];

    int[2][constLength] f = [[1,2],[1,2,3]];
    int[2][intLength] g = [[1,2],[1,2]];
    int[2][stringLength] h = [[1,2],[1,2]];
    int[2][length] i = [[1,2],[1,2]];
    int[2][2] j = [[1,2],[1,2,3]];

    int[constLength][2] k = [[1,2],[1,2],[1,2]];
    int[intLength][2] l = [[1,2],[1,2]];
    int[stringLength][2] m = [[1,2],[1,2]];
    int[length][2] n = [[1,2],[1,2]];
    int[2][2] o = [[1,2],[1,2],[1,2]];

    int[2][constLength][2] p = [[[1,2],[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][intLength][2] q = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][stringLength][2] r = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][length][2] s = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][2][2] t = [[[1,2],[1,2],[1,2]],[[1,2],[1,2]]];
    int["TEST"] testArr = [];
}
