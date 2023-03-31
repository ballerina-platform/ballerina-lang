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

function invalidSealedLiteralUsage() {
    int[*][*] a1 = [[1, 2], [3, 4]];
    int[][2][*] _ = [[[5, 6], [7, 8]], a1];
    int[*][2][*] _ = [[[5, 6], [7, 8]], a1];
    int[*][2][2] _ = [[[5, 5], [6, 6]]];

    int[][*]|string _ = "a1";
    int[][*][]|string _ = [[[5, 6], [7, 8]], a1];

    string[*][*] & readonly _ = [["1", "2"], ["3", "4"]];

    float[*] & readonly a2 = [3, 4];
    float[*][] & readonly|string _ = [[1, 2], a2];
    (float[][*]|string) & readonly _ = [[1, 2], a2];
    (float[2][2]) & readonly _ = [[3, 4], [3, 4]];
    (float[*][*]) & readonly _ = [[3, 4], [3, 4]];
}

function value1(int[*][*] a = [[1]]) returns int[*] {
    return [10];
}

public function invalidInferredArrays() {
    function (int[*][*] a = [[1]]) returns int[] _ = value1;
    var _ = function(int b) returns int {[int[*]] _ = [[1]]; int[*] _ = [32]; return b;};
    var _ = function(int[3] b) returns int[*] {return [2, 3];};

    boolean b3 = true;
    if (b3) {
        int[*] _ = [12,12];
    }
}

function fn1() returns int[*][*] & readonly {
    return [[21]];
}

function fn2() returns string[*][*]|string[1] {
    return [["1"]];
}

function fn3() returns int[] & readonly|string[*][]|int[*][*] {
    return [["1"]];
}

function fn4(int[*][] x = [[1]]) {
}

[int[*]] a2 = [[1]];
[string|int[*][], float] a3 = [[[1]], 1.2];
[int[*][] & readonly, float] a4 = [[[1]], 1.2];

map<int[*][]> a5 = {"1" : [[3]]};
map<float|int[*][*]> a6 = {"1" : [[3]]};
map<int[*][]> a7 = {};
var _ = function(int[3] b) returns int[*][] {return [[2, 3]];};
