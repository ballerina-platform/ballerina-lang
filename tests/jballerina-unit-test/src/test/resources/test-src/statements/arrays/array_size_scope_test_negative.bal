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

int[constLength] arr = [1, 2, 3];

const constLength = 2;
int intLength = 2;
const stringLength = "2";

int[constLength] a = [1, 2, 3];
int[intLength] b = [1, 2];
int[stringLength] c = [1, 2];
int[length] d = [1, 2];

function arraySizeReferenceInDifferentScopeTest() {
    int[constLength] e = [1, 2, 3];
    int[intLength] f = [1, 2];
    int[stringLength] g = [1, 2];
    int[length] h = [1, 2];

    function () anonFunction =
                function () {
                   int[constLength] i = [1, 2, 3];
                   int[intLength] j = [1, 2];
                   int[stringLength] k = [1, 2];
                   int[length] l = [1, 2];
                };

    anonFunction();

    int x = 10;

    if (x == 10) {
        int[constLength] m = [1, 2, 3];
        int[intLength] n = [1, 2];
        int[stringLength] o = [1, 2];
        int[length] p = [1, 2];
    }
}