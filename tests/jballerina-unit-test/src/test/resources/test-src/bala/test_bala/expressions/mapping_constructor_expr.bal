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

import testorg/foo;

type Rec record {
    string name;
};

function testModuleConstantsInMappingConstructor() {
    map<string> m1 = {[foo:MAPPING_A] : "A_VAL"};
    assertEquals("A_VAL", m1["A"]);
    map<string> m2 = {[foo:MAPPING_A] : foo:MAPPING_B};
    assertEquals("B", m2["A"]);
    map<string>? m3 = {[foo:MAPPING_A] : foo:MAPPING_B};
    assertEquals("B", m3["A"]);
    map<string>|Rec m4 = {[foo:MAPPING_A] : foo:MAPPING_B};
    assertEquals("B", m4["A"]);
    Rec r1 = {name: foo:MAPPING_A};
    assertEquals("A", r1.name);
    Rec? r2 = {name: foo:MAPPING_A};
    assertEquals("A", (<Rec>r2).name);
}

function assertEquals(anydata expected, anydata actual) {
    if expected != actual {
        panic error(string `expected ${expected.toBalString()}, found ${actual.toBalString()}`);
    }
}
