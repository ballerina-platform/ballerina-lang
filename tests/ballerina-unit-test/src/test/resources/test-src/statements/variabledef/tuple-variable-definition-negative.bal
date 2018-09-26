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

function testNegative1() {
    (string, int, float) (s, i, f) = ("D", 4, 6.7);
    (int, byte, boolean) (s, i, r, t) = (4, 6, true);
}

function testNegative2() {
    (string, int, float) (s1, i1) = ("D", 4, 6.7);
    (string, int, float) (s2, i2, r2, t2) = ("D", 4, 6.7);
    ((string, int), float) ((s3, (i3, r3)), f3) = (("D", 4), 6.7);
}

function testNegative3() {
    (string, int, float) (s1, i1, f1) = ("D", 4, 6.7, 45);
    (int, byte, boolean) (s2, i2, r2) = (4, 6);
    (string, int, float) (s3, i3, f3) = (4, 5.6, 3);
}
