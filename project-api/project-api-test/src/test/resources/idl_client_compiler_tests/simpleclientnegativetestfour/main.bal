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

client "http://example.com/invalidgeneratedcode/one.yaml" as foo;
foo:MyClientClass a = new;

client "http://example.com/invalidgeneratedcode/two.yaml" as bar;
bar:'client b = new;

function fn() {
    client "http://example.com/invalidgeneratedcode/three.yaml" as baz;
    baz:'client? _ = ();

    client "http://example.com/invalidgeneratedcode/four.yaml" as qux;
    qux:client _ = new;
}
