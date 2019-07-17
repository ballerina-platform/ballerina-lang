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
function testModuleLevelAccessOfAbstractObject() {
    ObjReferenceToAbstractObject abstractObj = new("string", 12, 100.0);

    test:assertEquals(abstractObj.publicStringField, "string", msg = "expected object public field to be accessible");

    test:assertEquals(abstractObj.getPrivateField(), 12,
        msg = "expected object private field to be accessible via object method");

    test:assertEquals(abstractObj.defaultVisibilityFloatField, 100.0,
        msg = "expected object default visibilty field to be accessible");

    test:assertEquals(abstractObj.defaultVisibiltyMethodDecl("argOne", 50), (),
        msg = "expected object default visibility method to be accessible");

    test:assertEquals(abstractObj.defaultVisibiltyMethodOutsideDecl("argOne", 25), (),
        msg = "expected object default visibility method declared outside to be accessible");

    test:assertEquals(abstractObj.publicMethodDecl("argOne", 125, 25), 325.0,
        msg = "expected object public visibility method to be accessible");
}

@test:Config {}
function testModuleLevelAccessOfAbstractClientObject() {
    ObjReferenceToAbstractClientObject clientObject = new("string", 12, 100.0);

    test:assertEquals(clientObject.publicStringField, "string",
        msg = "expected client object public field to be accessible");

    test:assertEquals(clientObject.getPrivateField(), 12,
        msg = "expected client object private field to be accessible via object method");

    test:assertEquals(clientObject.defaultVisibilityFloatField, 100.0,
        msg = "expected client object default visibilty field to be accessible");

    test:assertEquals(clientObject.defaultVisibiltyMethodDecl("argOne", 50), (),
        msg = "expected client object default visibility method to be accessible");

    test:assertEquals(clientObject.defaultVisibiltyMethodOutsideDecl("argOne", 25), (),
        msg = "expected client object default visibility method declared outside to be accessible");

    _ = clientObject->defaultVisibiltyRemoteMethodDecl("argOne", 25);
    test:assertEquals(clientObject.publicMethodDecl("argOne", 125, 25), 350.0,
        msg = "expected client object public visibility method to be accessible");

    var result = clientObject->remoteMethodOutsideDecl("argOne", 125);
    test:assertEquals(result, 475.0,
        msg = "expected client object public visibility remote method declared outside to be accessible");

    result = clientObject->publicRemoteMethodDecl("argOne", 125, 50);
    test:assertEquals(result, 650.0,
        msg = "expected client object public visibility remote method declared outside to be accessible");
}
