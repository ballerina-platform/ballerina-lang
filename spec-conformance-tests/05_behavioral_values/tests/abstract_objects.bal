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

// An abstract object type must not have an object-ctor-function and does
// not have an implicit initial value.
type AbstractObject abstract object {
    public string publicStringField;
    private int privateIntField;
    float defaultVisibilityFloatField;

    function getPrivateField() returns int;
    function defaultVisibiltyMethodDecl(string argOne, int argTwo);
    public function publicMethodDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float;
    function defaultVisibiltyMethodOutsideDecl(string argOne, int argTwo);
    //public function publicMethodOutsideDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float;
};

type ObjReferenceToAbstractObject object {
    *AbstractObject;

    function getPrivateField() returns int {
        return self.privateIntField;
    }

    function defaultVisibiltyMethodDecl(string argOne, int argTwo) {
        self.defaultVisibilityFloatField += argTwo;
    }

    public function publicMethodDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float {
        self.defaultVisibilityFloatField += defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }

    function __init(string argOne, int argTwo, float argThree) {
        self.publicStringField = argOne;
        self.privateIntField = argTwo;
        self.defaultVisibilityFloatField = argThree;
    }
};

type AbstractClientObject abstract client object {
    public string publicStringField;
    private int privateIntField;
    float defaultVisibilityFloatField;

    function getPrivateField() returns int;
    function defaultVisibiltyMethodDecl(string argOne, int argTwo);
    public function publicMethodDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float;
    function defaultVisibiltyMethodOutsideDecl(string argOne, int argTwo);
    remote function defaultVisibiltyRemoteMethodDecl(string argOne, int argTwo);
    public remote function publicRemoteMethodDecl(string argOne, int argTwo,
                                                  float defaultVisibilityFloatField) returns float;
    remote function remoteMethodOutsideDecl(string argOne, int argTwo) returns float;
};

function ObjReferenceToAbstractObject.defaultVisibiltyMethodOutsideDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

type ObjReferenceToAbstractClientObject client object {
    *AbstractClientObject;

    function getPrivateField() returns int {
        return self.privateIntField;
    }

    function defaultVisibiltyMethodDecl(string argOne, int argTwo) {
        self.defaultVisibilityFloatField += argTwo;
    }

    public function publicMethodDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float {
        self.defaultVisibilityFloatField += defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }

    remote function defaultVisibiltyRemoteMethodDecl(string argOne, int argTwo) {
        self.defaultVisibilityFloatField += argTwo;
    }

    public remote function publicRemoteMethodDecl(string argOne, int argTwo,
                                                  float defaultVisibilityFloatField) returns float {
        self.defaultVisibilityFloatField += defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }

    function __init(string argOne, int argTwo, float argThree) {
        self.publicStringField = argOne;
        self.privateIntField = argTwo;
        self.defaultVisibilityFloatField = argThree;
    }
};

function ObjReferenceToAbstractClientObject.defaultVisibiltyMethodOutsideDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

remote function ObjReferenceToAbstractClientObject.remoteMethodOutsideDecl(string argOne, int argTwo) returns float {
    self.defaultVisibilityFloatField += argTwo;
    return self.defaultVisibilityFloatField;
}

//public function ObjReferenceToAbstractObject.publicMethodOutsideDecl(
//                                      string argOne, int argTwo, float defaultVisibilityFloatField) returns float {
//    return self.defaultVisibilityFloatField;
//}
@test:Config {}
function testAbstractObjectTypeDescriptor() {
    ObjReferenceToAbstractClientObject abstractClientObj = new("string", 12, 100.0);
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


    test:assertEquals(abstractClientObj.publicStringField, "string",
        msg = "expected client object public field to be accessible");
    test:assertEquals(abstractClientObj.getPrivateField(), 12,
        msg = "expected client object private field to be accessible via object method");
    test:assertEquals(abstractClientObj.defaultVisibilityFloatField, 100.0,
        msg = "expected client object default visibilty field to be accessible");

    test:assertEquals(abstractClientObj.defaultVisibiltyMethodDecl("argOne", 50), (),
        msg = "expected client object default visibility method to be accessible");
    test:assertEquals(abstractClientObj.defaultVisibiltyMethodOutsideDecl("argOne", 25), (),
        msg = "expected client object default visibility method declared outside to be accessible");
    _ = abstractClientObj->defaultVisibiltyRemoteMethodDecl("argOne", 25);
    test:assertEquals(abstractClientObj.publicMethodDecl("argOne", 125, 25), 350.0,
        msg = "expected client object public visibility method to be accessible");
    var result = abstractClientObj->remoteMethodOutsideDecl("argOne", 125);
    test:assertEquals(result, 475.0,
        msg = "expected client object public visibility remote method declared outside to be accessible");
    result = abstractClientObj->publicRemoteMethodDecl("argOne", 125, 50);
    test:assertEquals(result, 650.0,
        msg = "expected client object public visibility remote method declared outside to be accessible");
}
