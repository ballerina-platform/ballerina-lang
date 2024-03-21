// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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

import testorg/foo;

function testIncompatibleAssignment() {
    [string, string, int...] _ = foo:l1;
    int[] _ = foo:l5;
}

function testInvalidUpdates() {
    var a = foo:l1;
    a[0] = "1";
    a.push("2");

    var b = foo:l7;
    b.push("l7");
}

function testUndefinedSymbolAccess() {
    _ = foo:l13;
    _ = foo:l14;
}

function testNonPublicConstAccess() {
    _ = foo:l10;
    _ = foo:l11;
    _ = foo:l12;
}
