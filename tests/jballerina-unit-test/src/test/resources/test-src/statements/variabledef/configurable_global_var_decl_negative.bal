// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Configurable variable must be initialized
configurable int x;
// Configurable variable cannot be declared with var
configurable var a = 5;
// Configurable variable type must be a subtype of anydata.
configurable object {int a;} b = object {int a = 5;};
// Configurable variable type must be a subtype of readonly.
configurable record {int c;} d = {c:5};

function foo() {
    // Cannot declare configurable variable locally
    configurable int e = 6;
}
// TODO: remove this after runtime supports all configurable types
configurable decimal f = ?;

// TODO: validate configurable var decl for tuple, record and error var once they are supported.
