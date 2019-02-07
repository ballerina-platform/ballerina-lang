// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// All basic types of structural values, with the exception of the XML, are mutable,
// meaning the value referred to by a particular reference can be changed.
// Non-XML types are already tested.
@test:Config {}
function testXmlImmutability() {
    xml x1 = xml `<book/>`;
    xml x2 = x1;
    xml x3 = xml `<name>Book1</name>`;
    x1 = x1 + x3;
    test:assertTrue(x1 !== x2, msg = "expected mutated xml to be a new value");
}
