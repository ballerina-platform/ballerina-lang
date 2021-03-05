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

function testTupleForeachNegative1() {
    [int, int, string, int, string] ldata = [10, 5, "string", 3, "string"];
    int i = 0;
    foreach string v in ldata {

    }
    foreach int v in ldata {

    }
}

function testTupleForeachNegative2() {
    [int, int, string, int, boolean] ldata = [10, 5, "string", 3, false];
    int i = 0;
    foreach string|int v in ldata {

    }
}

function testTupleForeachNegative3() {
    [[int, int...]...] x = [[1, 2], [3, 4, 5, 6], [7, 8], [9, 10]];
    foreach [int, int, int...] [a, ...b] in x {

    }
}

function testTupleForeachNegative4() {
    [[int, int...], [int, int, int...], int[2]] x = [[1, 2], [3, 4, 5, 6], [7, 8]];
    foreach [int, int, int...] [a, ...b] in x {

    }
}

function testTupleForeachNegative5() {
    [[int, int...], [int, int, int...], int] x = [[1, 2], [3, 4, 5, 6], 12];
    foreach var [a, ...b] in x {
    }
}

function testTupleForeachNegative6() {
    [[int, int...], [int, int, int...]] x = [[1, 2], [3, 4, 5, 6]];
    foreach var [a] in x {
    }
}
