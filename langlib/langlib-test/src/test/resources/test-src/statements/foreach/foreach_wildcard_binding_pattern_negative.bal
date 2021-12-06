// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testWildcardBindingPatternInForeachStatementNegative() {
    error?[] x1 = [];

    foreach error? _ in x1 {
    }

    foreach var _ in x1 {
    }

    map<error> x2 = {};

    foreach error _ in x2 {
    }

    foreach var _ in x2 {
    }

    // https://github.com/ballerina-platform/ballerina-lang/issues/33544
    error<record { error a; }>[] x3 = [];
    foreach var error(m1, a1 = _) in x3 { // should result in an error
    }

    foreach error<record { int|error a; }> error(m2, a2 = _) in x3 {  // should result in an error
    }

    map<record {| error? x; int y; |}> x4 = {};
    foreach var {x: _, y} in x4 {
    }

    foreach record {| error? x; int y; int z?; |} {x: _, y} in x4 {
    }
}
