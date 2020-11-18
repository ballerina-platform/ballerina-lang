// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import testorg/foo;
import testorg/utils;


distinct class Bar {
    *foo:DBar;
    int j;

    function init(int i, int j) {
        self.i = i;
        self.j = j;
    }
}

public function testDistinctAssignability() {
    foo:DBar b = new(1);
    foo:DFoo f = b; // Can assign value of distinct subtype to a super type.
    utils:assertEquality(1, f.i);

    foo:DA x = new(11, 22);
    foo:DB y = x;
    utils:assertEquality(11, y.i);
    utils:assertEquality(22, y.j);

    foo:DY w = new(33);
    foo:DX z = w; // Can assign value of distinct subtype to a non distinct super type.
    utils:assertEquality(33, z.i);

    Bar p = new(100, 1000);
    foo:DBar q = p;
    utils:assertEquality(100, q.i);
}
