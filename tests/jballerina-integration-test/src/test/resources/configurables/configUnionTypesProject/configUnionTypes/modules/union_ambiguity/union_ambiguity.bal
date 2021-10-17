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

configurable decimal|string decimalVar = ?;
configurable float|int floatVar = ?;
configurable float|decimal|string unionVar1 = ?;

configurable xml:Element|int xmlVar = ?;
configurable string|boolean stringVar = ?;
configurable xml:Comment|string|boolean unionVar2 = ?;

configurable map<anydata>[]|int mapArrayVar = ?;
configurable table<map<anydata>>|int tableVar = ?;
configurable table<map<anydata>>|map<anydata>[]|int unionVar3 = ?;

public function test_ambiguous_union_type() {
    test:assertEquals(decimalVar, <decimal>34.56);
    test:assertEquals(floatVar, 78.91);
    test:assertEquals(unionVar1, "This is a union variable");

    test:assertEquals(xmlVar, xml `<name>Jane Doe</name>`);
    test:assertEquals(stringVar, "This is a string");
    test:assertEquals(unionVar2, true);

    test:assertEquals(mapArrayVar.toString(), "[{\"a\":\"test string\",\"b\":12,\"c\":false}]");
    test:assertEquals(tableVar.toString(), "[{\"a\":\"Hello World!\",\"b\":34,\"c\":true}]");
    test:assertEquals(unionVar3, 24);
}
