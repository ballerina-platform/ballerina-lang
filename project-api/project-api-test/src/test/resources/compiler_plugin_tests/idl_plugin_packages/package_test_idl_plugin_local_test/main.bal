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
import package_test_idl_plugin.mod1 as mod1;

public function main() {
    _ = mod1:testModuleClientDecl();
}

client "<<IDL_ABSOLUTE_PATH>>" as foo;
client "<<IDL_FILE_PROTOCOL>>" as bar;
client "./projectapiclientplugin.json" as baz;

function testModuleClientDecl() returns string {
    foo:ClientConfiguration config = {specVersion : "3.0.0"};
    foo:client cl = new (config);
    return cl->getSpecVersion();
}

function testModuleClientDecl1() returns string {
    bar:ClientConfiguration config = {specVersion : "3.0.0"};
    bar:client cl = new (config);
    return cl->getSpecVersion();
}

function testModuleClientDecl2() returns string {
    baz:ClientConfiguration config = {specVersion : "3.0.0"};
    baz:client cl = new (config);
    return cl->getSpecVersion();
}
