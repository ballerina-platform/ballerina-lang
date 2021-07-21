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

import ballerina/test;
import testorg/runtime_api_types.objects;

public function main() {
    objects:PublicClientObject obj = new ();
    [string,boolean] [] parameters = objects:getParameters(obj, "getRemoteCounter");
    test:assertEquals(parameters.length(), 3);
    test:assertEquals(parameters[0], ["num", false]);
    test:assertEquals(parameters[1], ["value", false]);
    test:assertEquals(parameters[2], ["msg", true]);
}
