//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//Test duplicate module level var declaration inside tuple binding pattern
int a = 5;
[int, float, string] [a, b, c] = [1, 2.5, "Mac"];
float b = 6;
public function testBasic() returns [int, float, string] {
    // Access un declared module variable
    while (d < 3) {
        d += 1;
    }
    // Redeclare global variable locally
    string c = "Duplicate string";
    return [a, b, c];
}
// Only simple variables are allowed to be isolated
isolated [int, string] [e, f] = [5, "Jhone"];

const annotation annot on source function;

@annot
[int, int] [j, k] = [1, 2];

[int, int] [l, m] = [4, o];
var [n, o] = getVarValues();
boolean n = false;

function getVarValues() returns [boolean, int] {
    return [true, 43];
}
