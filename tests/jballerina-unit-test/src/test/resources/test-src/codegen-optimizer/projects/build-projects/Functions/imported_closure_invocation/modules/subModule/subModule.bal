// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

public function closureReturn() returns (function () returns string) {
    var closureUsed = function() returns string {
        return foo();
    };

    var closureUnused = function() returns string {
        return bar();
    };
    return closureUsed;
}

function foo() returns string {
    return "Foo";
}

function bar() returns string {
    return "Bar";
}
