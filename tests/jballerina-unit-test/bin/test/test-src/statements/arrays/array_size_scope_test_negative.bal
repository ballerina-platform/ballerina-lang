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
    int[constLength] _ = [1, 2, 3];
    int[intLength] _ = [1, 2];
    int[stringLength] _ = [1, 2];
    int[length] _ = [1, 2];

    function () anonFunction =
                function () {
                   int[constLength] _ = [1, 2, 3];
                   int[intLength] _ = [1, 2];
                   int[stringLength] _ = [1, 2];
                   int[length] _ = [1, 2];
                };

    anonFunction();

    int x = 10;

    if (x == 10) {
        int[constLength] _ = [1, 2, 3];
        int[intLength] _ = [1, 2];
        int[stringLength] _ = [1, 2];
        int[length] _ = [1, 2];
    }
}