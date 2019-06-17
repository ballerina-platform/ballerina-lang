// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function initTwoDimensionalSealedArray() returns [int, int, int, int] {
    string[3][4] x1 = [["00", "01", "02", "03"], ["10", "11", "12", "13"], ["20", "21", "22", "23"]];
    float[*][*] x2 = [[1.0, 2.1, 3.2], [4.3, 5.4, 6.5]];

    return [x1.length(), x1[0].length(), x2.length(), x2[0].length()];
}

function initThreeDimensionalSealedArray() returns [int, int, int, int, int, int] {
    boolean[3][4][5] x1 = [[[false, true, false, true, false], [false, true, false, true, false],
    [false, true, false,true, false], [false, true, false, true, false]], [[false, true, false, true, false],
    [false, true, false, true, false], [false, true, false,true, false], [false, true, false, true, false]],
    [[false, true, false, true, false], [false, true, false, true, false], [false, true, false,true, false],
    [false, true, false, true, false]]];
    int[*][*][*] x2 = [[[1, 2, 3], [4, 5, 6], [7, 8, 9]], [[1, 2, 3], [4, 5, 6], [7, 8, 9]]];

    return [x1.length(), x1[0].length(), x1[0][0].length(), x2.length(), x2[0].length(), x2[0][0].length()];
}

function twoDArrayIntAssignment(int[2] x2) returns [int, int, int] {
    int[*] x1 = [3, 2];
    int[3][2] xx1 = [x1, x2, x1];

    return [xx1[0][0], xx1[1][0], xx1[2][0]];
}

function twoDArrayStringAssignment(string[2] x2) returns [string, string, string] {
    string[*] x1 = ["val1", "val2"];
    string[3][2] xx1 = [x1, x2, x1];

    return [xx1[0][0], xx1[1][0], xx1[2][0]];
}
