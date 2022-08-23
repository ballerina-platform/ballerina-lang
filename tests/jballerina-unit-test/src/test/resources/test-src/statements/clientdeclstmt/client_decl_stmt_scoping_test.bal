// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

client "http://www.example.com/apis/one.yaml" as foo;

function f1() {
    client "http://www.example.com/apis/two.yaml" as foo; // OK, shadows the module-level prefix
}

function f2(boolean b) {
    if b {
        client "http://www.example.com/apis/one.yaml" as bar; // OK, different scope
    } else {
        client "http://www.example.com/apis/one.yaml" as bar; // OK, different scope
    }

    if !b {
        client "http://www.example.com/apis/one.yaml" as bar; // OK, different scope
        return;
    }
    client "http://www.example.com/apis/one.yaml" as bar; // OK, different scope
}
