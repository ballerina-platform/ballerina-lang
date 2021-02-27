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

import ballerina/test;
import configPkg.util.foo;

configurable int intVar = 5;
configurable float floatVar = 9.5;
configurable string stringVar = ?;
configurable boolean booleanVar = ?;

public function main() {
    test:assertEquals(42, intVar);
    test:assertEquals(3.5, floatVar);
    test:assertEquals("abc", stringVar);
    test:assertEquals(true, booleanVar);

    //from imported module
    test:assertEquals(14.4, foo:getAverage());
    test:assertEquals("world", foo:getString());
    test:assertEquals(false, foo:getBoolean());

    print("Tests passed");
}
