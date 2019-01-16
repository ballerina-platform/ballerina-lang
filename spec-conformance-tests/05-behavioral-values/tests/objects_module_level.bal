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

// Visibility of fields, object initializer and methods is specified uniformly: public means that
// access is unrestricted; private means that access is restricted to the same object; if no
// visibility is specified explicitly, then access is restricted to the same module.
@test:Config {}
function testObjectTypeDescriptorInSameModule() {
    ClientObject clientObject2 = new("default string value", 12, 100.0);
    NormalObject normalObject = new("default string value", 12, 100.0);
    test:assertEquals(normalObject.publicStringField, "default string value",
        msg = "expected object public field to be accessible");
    test:assertEquals(normalObject.getPrivateField(), 12,
        msg = "expected object private field to be accessible via an object method");
    test:assertEquals(normalObject.defaultVisibilityFloatField, 100.0, msg = "expected object public field");

    test:assertEquals(normalObject.defaultVisibiltyMethodDecl("argOne", 50), 150.0, msg =
        "expected object public field");
    test:assertEquals(normalObject.publicMethodDefn("argOne", 100, 50.0), 250.0, msg = "expected object public field");
    test:assertEquals(normalObject.defaultVisibiltyMethodDefn("argOne", 100), 350.0,
        msg = "expected object public field");

    test:assertEquals(clientObject2.publicStringField, "default string value",
        msg = "expected object public field to be accessible");
    test:assertEquals(clientObject2.getPrivateField(), 12,
        msg = "expected object private field to be accessible via an object method");
    test:assertEquals(clientObject2.defaultVisibilityFloatField, 100.0, msg = "expected object public field");

    test:assertEquals(clientObject2.defaultVisibiltyMethodDecl("argOne", 50), 150.0, msg =
        "expected object public field");

    _ = clientObject2->defaultVisibiltyRemoteMethodDecl("argOne", 50);
    test:assertEquals(clientObject2.defaultVisibilityFloatField, 200.0, msg = "expected object public field");

    test:assertEquals(clientObject2.publicMethodDefn("argOne", 100, 150), 450.0, msg = "expected object public field");
    test:assertEquals(clientObject2.defaultVisibiltyMethodDefn("argOne", 50), 500.0,
        msg = "expected object public field");
}
