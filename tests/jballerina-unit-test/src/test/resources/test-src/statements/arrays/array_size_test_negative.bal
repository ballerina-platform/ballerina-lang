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
    int[constLength] _ = [1, 2, 3];
    int[intLength] _ = [1, 2];
    int[stringLength] _ = [1, 2];
    int[length] _ = [1, 2];
    int[2] _ = [1, 2, 3];

    int[2][constLength] _ = [[1,2],[1,2,3]];
    int[2][intLength] _ = [[1,2],[1,2]];
    int[2][stringLength] _ = [[1,2],[1,2]];
    int[2][length] _ = [[1,2],[1,2]];
    int[2][2] _ = [[1,2],[1,2,3]];

    int[constLength][2] _ = [[1,2],[1,2],[1,2]];
    int[intLength][2] _ = [[1,2],[1,2]];
    int[stringLength][2] _ = [[1,2],[1,2]];
    int[length][2] _ = [[1,2],[1,2]];
    int[2][2] _ = [[1,2],[1,2],[1,2]];

    int[2][constLength][2] _ = [[[1,2],[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][intLength][2] _ = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][stringLength][2] _ = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][length][2] _ = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[2][2][2] _ = [[[1,2],[1,2],[1,2]],[[1,2],[1,2]]];
    int["TEST"] _ = [];

    int[2147483638] _ = [];
    int[int:MAX_VALUE] _ = [];

    int[9223372036854775808] _ = [];
    int[0x001234567890abcdef01] _ = [];
}

const negativeLength = -1;

function arrayNegativeSizeTest() {
    int[negativeLength] z = [];
}
