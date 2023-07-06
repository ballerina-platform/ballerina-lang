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

function arraySizeTest() {
    int[constLength] _ = [1,2];
    int[2][constLength] f = [[1,2],[1,2]];
    int[constLength][2] k = [[1,2],[1,2]];
    int[2][constLength][2] _ = [[[1,2],[1,2]],[[1,2],[1,2]]];
    int[0x2] q = [1, 2];
    int[0x00012345678] r;
    int[2] s = q;
    int[0x2][2] t = f;
    int[0x2][0x2] u = k;
    int[0] v;
    int[2147483637] w;
}
