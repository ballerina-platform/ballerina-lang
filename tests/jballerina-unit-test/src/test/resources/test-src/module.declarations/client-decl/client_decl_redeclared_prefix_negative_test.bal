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

import ballerina/lang.'int as foo;
import ballerina/lang.'string as bar;
import ballerina/lang.'float as baz;

client "http://www.example.com/apis/one.yaml" as foo;

client "http://www.example.com/apis/two.yaml" as bar;
client "http://www.example.com/apis/three.yaml" as bar;

client "http://www.example.com/apis/four.yaml" as baz;
xmlns "http://example.com" as baz;

client "http://www.example.com/apis/six.yaml" as qux;
client "http://www.example.com/apis/seven.yaml" as qux;
xmlns "http://example.com" as qux;

client "http://www.example.com/apis/nigth.yaml" as quux;
client "http://www.example.com/apis/ten.yaml" as quux;

xmlns "http://example.com" as corge;
client "http://www.example.com/apis/ten.yaml" as corge;

public function main() {
    foo:Signed8 _ = 1;
    bar:Char _ = "a";
}
