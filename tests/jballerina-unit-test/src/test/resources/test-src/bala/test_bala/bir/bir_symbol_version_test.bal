// Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
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

import testorg/bir_symbol_version_dep as dep;
import testorg/bir_symbol_version_lib as lib;

final readonly & dep:Person2 person = {
    name: "John Doe",
    age: 30,
    status: {status: lib:ACTIVE}
};

// Verifies that when loading BIR from a bala, package IDs for transitive
// dependencies are resolved to the version in the current import symbols
// rather than the version embedded in the BIR constant pool.
function testBIRSymbolVersionResolution() {
    var status = dep:getStatus();
    assertEquality("ACTIVE", status);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
