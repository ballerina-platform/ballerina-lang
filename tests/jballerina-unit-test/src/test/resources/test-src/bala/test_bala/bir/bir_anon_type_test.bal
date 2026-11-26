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

import testorg/bir_anon_type_dep as dep;

// Verifies that a package loaded from its BIR can refer to the anonymous types of a
// dependency that is compiled from sources.
function testLoadBIRWithSourceCompiledDependency() {
    string name = dep:getName({config: {name: "tool"}});
    if name != "tool" {
        panic error("expected 'tool', found '" + name + "'");
    }
}
