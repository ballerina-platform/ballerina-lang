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
import objects;

@test:Config {}
function testPublicObjectAccessWithNormalObjects() {
    objects:PublicNormalObject normalObject = new("default string");

    test:assertEquals(normalObject.publicStringField, "default string",
        msg = "expected object public field to be accessible");

    test:assertEquals(normalObject.publicMethodDefn(), 20,
        msg = "expected object public method to be accessible");

    test:assertEquals(normalObject.publicMethodDecl(), 30,
        msg = "expected object public method to be accessible");
}

@test:Config {}
function testPublicObjectAccessWithClientObjects() {
    objects:PublicClientObject clientObject = new("default string");

    test:assertEquals(clientObject.publicStringField, "default string",
        msg = "expected client object public field to be accessible");

    test:assertEquals(clientObject.publicMethodDefn(), 20,
        msg = "expected client object public method to be accessible");

    int result = clientObject->publicRemoteMethodDefn("changed string");
    test:assertEquals(result, 30, msg = "expected client object public remote method to be accessible");

    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be updated");

    test:assertEquals(clientObject.publicMethodDecl(), 40,
        msg = "expected client object public method to be accessible");

    result = clientObject->publicRemoteMethodDecl();
    test:assertEquals(result, 50,
        msg = "expected client object public method to be accessible");
}

@test:Config {}
function testPublicObjectAccessIncludingObjectReference() {
    objects:ObjReferenceToAbstractObject normalObject = new("default string");

    test:assertEquals(normalObject.publicStringField, "default string",
        msg = "expected object public field to be accessible");

    test:assertEquals(normalObject.publicMethodDecl("string value"), 20,
        msg = "expected object public method to be accessible");

    test:assertEquals(normalObject.getPrivateField(), 20,
        msg = "expected object private field to be accessed via public method");

    test:assertEquals(normalObject.publicStringField, "string value",
        msg = "expected object public field to be accessible");

    test:assertEquals(normalObject.publicMethodDeclaredOutside(), 30,
        msg = "expected object public method to be accessible");
}

@test:Config {}
function testPublicClientObjectAccessIncludingObjectReference() {
    objects:ObjReferenceToAbstractClientObject clientObject = new("default string");

    test:assertEquals(clientObject.publicStringField, "default string",
        msg = "expected client object public field to be accessible");

    test:assertEquals(clientObject.publicMethodDecl(), 20,
        msg = "expected client object public field to be accessible");

    int result = clientObject->publicRemoteMethodDecl("changed string");
    test:assertEquals(result, 30, msg = "expected client object public remote method to be accessible");

    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be updated");

    test:assertEquals(clientObject.publicMethodDeclaredOutside(), 40,
        msg = "expected client object public method to be accessible");

    result = clientObject->publicRemoteMethodDeclaredOutside();
    test:assertEquals(result, 50, msg = "expected client object public method to be accessible");
}

client class ObjReferenceToPublicAbstractClientObject {
    *objects:AbstractClientObject;
    private int counter;

    public function getPrivateField() returns int {
        return self.counter;
    }

    public function publicMethodDecl() returns int {
        self.counter += 10;
        return self.counter;
    }

    public remote function publicRemoteMethodDecl(string argOne) returns int {
        self.publicStringField = argOne;
        self.counter += 10;
        return self.counter;
    }

    public function init(string argOne) {
        self.publicStringField = argOne;
        self.counter = 10;
    }
}

public function ObjReferenceToPublicAbstractClientObject.publicMethodDeclaredOutside() returns int {
    self.counter += 10;
    return self.counter;
}

public remote function ObjReferenceToPublicAbstractClientObject.publicRemoteMethodDeclaredOutside() returns int {
    self.counter += 10;
    return self.counter;
}


@test:Config {}
function testPublicObjectAccessIncludingPublicObjectReference() {
    ObjReferenceToPublicAbstractClientObject clientObject = new("default string");

    test:assertEquals(clientObject.publicStringField, "default string",
        msg = "expected client object public field to be accessible");

    test:assertEquals(clientObject.publicMethodDecl(), 20,
        msg = "expected client object public method to be accessible");

    int result = clientObject->publicRemoteMethodDecl("changed string");
    test:assertEquals(result, 30, msg = "expected client object public remote method to be accessible");

    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be updated");

    test:assertEquals(clientObject.publicMethodDeclaredOutside(), 40,
        msg = "expected client object public method to be accessible");

    result = clientObject->publicRemoteMethodDeclaredOutside();
    test:assertEquals(result, 50, msg = "expected client object public method to be accessible");
}
