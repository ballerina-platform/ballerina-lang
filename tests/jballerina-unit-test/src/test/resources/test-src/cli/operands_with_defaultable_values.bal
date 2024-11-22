// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/test;

type INT int;

public function main(int x, int? y, int a = 3, string s = "abc", float f = 5.5, INT i = 10, int:Signed16 j = 12) {
    test:assertEquals(x, 10);
    test:assertEquals(y, 12);
    test:assertEquals(a, 0);
    test:assertEquals(s, "");
    test:assertEquals(f, 0.0);
    test:assertEquals(i, 0);
    test:assertEquals(j, 0);
}
