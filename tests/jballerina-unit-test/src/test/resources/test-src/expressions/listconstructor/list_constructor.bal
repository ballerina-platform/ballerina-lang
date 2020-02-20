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

function testListConstructorExpr() returns boolean {
    var fooList = [
        ["AAA", 111],
        ["BBB", 222]
    ];

    foreach var foo in fooList {}

    var barList = ["CCC", 333];

    (int|string)[] fooArr = ["DDD", 444];

    return fooList[0][0] == "AAA"
        && fooList[0][1] == 111
        && fooList[1][0] == "BBB"
        && fooList[1][1] == 222
        && barList[0] == "CCC"
        && barList[1] == 333
        && fooArr[0] == "DDD"
        && fooArr[1] == 444;
}

function testListConstructorAutoFillExpr() {
    int[8] arrOfEightInts = [1, 2, 3];
    int sum = 0;
    int i = 0;

    while (i < 8) {
        sum += arrOfEightInts[i];
        i = i + 1;
    }

    if (sum != 6) {
        panic error("Invalid sum of int array");
    }
}
