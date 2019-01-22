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
import utils;

// Objects are a combination of public and private fields along with a set of associated
// functions, called methods, that can be used to manipulate them. An object’s methods are
// associated with the object when the object is constructed and cannot be changed thereafter.

// object-type-descriptor := object-type-quals object { object-member-descriptor* }
// object-type-quals := [abstract] [client] | [client] abstract
// object-member-descriptor := object-field-descriptor | object-method | object-type-reference
type EmptyObject object {

};

type NormalObject object {
    // object-field-descriptor := [visibility-qual] type-descriptor field-name ;
    public string publicStringField;
    private int privateIntField;
    float defaultVisibilityFloatField;

    function __init(string argOne, int argTwo, float argThree) {
        self.publicStringField = argOne;
        self.privateIntField = argTwo;
        self.defaultVisibilityFloatField = argThree;
    }

    function getPrivateField() returns int {
        return self.privateIntField;
    }

    // object-method := method-decl | method-defn
    // method-decl :=
    // metadata
    // [visibility-qual][remote]
    // function method-name function-signature ;
    // method-defn :=
    // metadata
    // method-defn-quals
    // function method-name function-signature method-body
    // method-defn-quals := [extern] [visibility-qual] [remote] [visibility-qual] extern [remote]
    // method-name := identifier
    // method-body := function-body-block | ;
    // metadata := [DocumentationString] annots
    public function publicMethodDecl(string argOne, int argTwo) returns float;
    private function privateMethodDecl(string argOne, int argTwo);
    function defaultVisibiltyMethodDecl(string argOne, int argTwo) returns float;

    public function publicMethodDefn(string argOne, int argTwo, float defaultVisibilityFloatField) returns float {
        if (defaultVisibilityFloatField == self.defaultVisibilityFloatField) {
            return 0.0;
        }

        self.defaultVisibilityFloatField = self.defaultVisibilityFloatField + defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }

    private function privateMethodDefn(string argOne, int argTwo) {
        self.defaultVisibilityFloatField = self.defaultVisibilityFloatField + argTwo;
    }

    function defaultVisibiltyMethodDefn(string argOne, int argTwo) returns float {
        self.defaultVisibilityFloatField = self.defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }
};

public function NormalObject.publicMethodDecl(string argOne, int argTwo) returns float {
    self.publicStringField = argOne;
    self.defaultVisibilityFloatField += argTwo;
    return self.defaultVisibilityFloatField;
}

private function NormalObject.privateMethodDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

function NormalObject.defaultVisibiltyMethodDecl(string argOne, int argTwo) returns float {
    return self.defaultVisibilityFloatField + argTwo;
}

type ClientObject client object {
    public string publicStringField;
    private int privateIntField;
    float defaultVisibilityFloatField;

    function __init(string argOne, int argTwo, float argThree) {
        self.publicStringField = argOne;
        self.privateIntField = argTwo;
        self.defaultVisibilityFloatField = argThree;
    }

    function getPrivateField() returns int {
        return self.privateIntField;
    }

    public function publicMethodDecl(string argOne, int argTwo) returns float;
    private function privateMethodDecl(string argOne, int argTwo);
    function defaultVisibiltyMethodDecl(string argOne, int argTwo) returns float;

    public remote function publicRemoteMethodDecl(string argOne, int argTwo) returns float;
    private remote function privateRemoteMethodDecl(string argOne, int argTwo);
    remote function defaultVisibiltyRemoteMethodDecl(string argOne, int argTwo);

    public function publicMethodDefn(string argOne, int argTwo, float defaultVisibilityFloatField) returns float {
        if (defaultVisibilityFloatField == self.defaultVisibilityFloatField) {
            return 0.0;
        }

        self.defaultVisibilityFloatField += defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }

    private function privateMethodDefn(string argOne, int argTwo) {
        self.defaultVisibilityFloatField = self.defaultVisibilityFloatField + argTwo;
    }

    function defaultVisibiltyMethodDefn(string argOne, int argTwo) returns float {
        return self.defaultVisibilityFloatField + argTwo;
    }

    public remote function publicRemoteMethodDefn(string argOne, int argTwo,
                                                  float defaultVisibilityFloatField) returns float {
        if (defaultVisibilityFloatField == self.defaultVisibilityFloatField) {
            return 0.0;
        }

        self.defaultVisibilityFloatField += defaultVisibilityFloatField + argTwo;
        return self.defaultVisibilityFloatField;
    }

    private remote function privateRemoteMethodDefn(string argOne, int argTwo) {
        self.defaultVisibilityFloatField = self.defaultVisibilityFloatField + argTwo;
    }

    remote function defaultVisibiltyRemoteMethodDefn() returns float {
        return self.defaultVisibilityFloatField;
    }
};

public function ClientObject.publicMethodDecl(string argOne, int argTwo) returns float {
    self.publicStringField = argOne;
    self.defaultVisibilityFloatField += argTwo;
    return self.defaultVisibilityFloatField;
}

private function ClientObject.privateMethodDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

function ClientObject.defaultVisibiltyMethodDecl(string argOne, int argTwo) returns float {
    self.defaultVisibilityFloatField += argTwo;
    return self.defaultVisibilityFloatField;
}

public remote function ClientObject.publicRemoteMethodDecl(string argOne, int argTwo) returns float {
    self.publicStringField = argOne;
    self.defaultVisibilityFloatField += argTwo;
    return self.defaultVisibilityFloatField;
}

private remote function ClientObject.privateRemoteMethodDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

remote function ClientObject.defaultVisibiltyRemoteMethodDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

@test:Config {}
function testObjectTypeDescriptor() {
    ClientObject clientObject = new("default string value", 12, 100.0);
    EmptyObject emptyObject = new;
    NormalObject normalObject = new("default string value", 12, 100.0);

    test:assertEquals(normalObject.publicStringField, "default string value",
        msg = "expected object public field to be accessible");
    test:assertEquals(normalObject.getPrivateField(), 12,
        msg = "expected object private field to be accessible via an object method");
    test:assertEquals(normalObject.defaultVisibilityFloatField, 100.0,
        msg = "expected object default visibility field to be accessible");

    test:assertEquals(normalObject.defaultVisibiltyMethodDecl("argOne", 50), 150.0,
        msg = "expected object default visibility method to be accessible");
    test:assertEquals(normalObject.publicMethodDefn("argOne", 100, 50.0), 250.0,
        msg = "expected object public method to be accessible");
    test:assertEquals(normalObject.publicMethodDecl("changed string", 100), 350.0,
        msg = "expected object public method to be accessible");
    test:assertEquals(normalObject.publicStringField, "changed string",
        msg = "expected object public field to be accessible");
    test:assertEquals(normalObject.defaultVisibiltyMethodDefn("argOne", 50), 400.0,
        msg = "expected object default visibility method to be accessible");

    test:assertEquals(clientObject.publicStringField, "default string value",
        msg = "expected client object public field to be accessible");
    test:assertEquals(clientObject.getPrivateField(), 12,
        msg = "expected client object private field to be accessible via an object method");
    test:assertEquals(clientObject.defaultVisibilityFloatField, 100.0,
        msg = "expected client object default visibility field to be accessible");

    test:assertEquals(clientObject.defaultVisibiltyMethodDecl("argOne", 50), 150.0,
        msg = "expected client object public visibility method to be accessible");

    _ = clientObject->defaultVisibiltyRemoteMethodDecl("argOne", 50);
    test:assertEquals(clientObject.defaultVisibilityFloatField, 200.0,
        msg = "expected client object default visibility field to be accessible");
    _ = clientObject->publicRemoteMethodDecl("argOne", 50);
    test:assertEquals(clientObject.defaultVisibilityFloatField, 250.0,
        msg = "expected client object default visibility field to be accessible");

    test:assertEquals(clientObject.publicMethodDefn("argOne", 100, 150), 500.0,
        msg = "expected client object public method to be accessible");
    test:assertEquals(clientObject.publicMethodDecl("changed string", 50), 550.0,
        msg = "expected client object public method to be accessible");
    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be accessible");
    test:assertEquals(clientObject.defaultVisibiltyMethodDefn("argOne", 25), 575.0,
        msg = "expected client object default visibility method to be accessible");
}

@test:Config {}
function testPublicObjectAccess() {
    utils:PublicClientObject clientObject = new("default string");
    utils:PublicNormalObject normalObject = new("default string");

    test:assertEquals(normalObject.publicStringField, "default string",
        msg = "expected object public field to be accessible");
    test:assertEquals(normalObject.publicMethodDefn(), 20,
        msg = "expected object public method to be accessible");

    test:assertEquals(clientObject.publicStringField, "default string",
        msg = "expected client object public field to be accessible");
    test:assertEquals(clientObject.publicMethodDefn(), 20,
        msg = "expected client object public field to be accessible");
    int result = clientObject->publicRemoteMethodDefn("changed string");
    test:assertEquals(result, 30, msg = "expected client object public remote method to be accessible");
    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be updated");
}

@test:Config {}
function testPublicObjectAccessIncludingObjectReference() {
    utils:ObjReferenceToAbstractClientObject clientObject = new("default string");
    utils:ObjReferenceToAbstractObject normalObject = new("default string");

    test:assertEquals(normalObject.publicStringField, "default string",
        msg = "expected object public field to be accessible");
    test:assertEquals(normalObject.publicMethodDecl("string value"), 20,
        msg = "expected object public method to be accessible");
    test:assertEquals(normalObject.getPrivateField(), 20,
        msg = "expected object private field to be accessed via public method");
    test:assertEquals(normalObject.publicStringField, "string value",
        msg = "expected object public field to be accessible");

    test:assertEquals(clientObject.publicStringField, "default string",
        msg = "expected client object public field to be accessible");
    test:assertEquals(clientObject.publicMethodDecl(), 20,
        msg = "expected client object public field to be accessible");
    int result = clientObject->publicRemoteMethodDecl("changed string");
    test:assertEquals(result, 30, msg = "expected client object public remote method to be accessible");
    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be updated");
}

type ObjReferenceToPublicAbstractClientObject client object {
    *utils:AbstractClientObject;
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

    public function __init(string argOne) {
        self.publicStringField = argOne;
        self.counter = 10;
    }
};

@test:Config {}
function testObjectAccessIncludingPublicObjectReference() {
    ObjReferenceToPublicAbstractClientObject clientObject = new("default string");
    test:assertEquals(clientObject.publicStringField, "default string",
        msg = "expected client object public field to be accessible");
    test:assertEquals(clientObject.publicMethodDecl(), 20,
        msg = "expected client object public method to be accessible");
    int result = clientObject->publicRemoteMethodDecl("changed string");
    test:assertEquals(result, 30, msg = "expected client object public remote method to be accessible");
    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = "expected client object public field to be updated");
}

// A non-abstract object type provides a way to initialize an object of the type. An object is
// initialized by:
// 1. allocating storage for the object
// 2. initializing each field with its implicit initial value, if there is one defined for the type of the field
// 3. initializing the methods of the object using the type’s method definitions
// 4. calling the object’s __init method, if there is one
type InitMethodInObject object {
    string stringField;
    float[] floatArrayField;

    public function __init() {
        self.stringField = "string field";
        self.floatArrayField = [1.0, 5.0, 10.0];
    }
};

@test:Config {}
function testInitMethodInObject() {
    InitMethodInObject obj = new;
    test:assertEquals(obj.stringField, "string field", msg = "expected object field to have initialzed value");
    float[] expArray = [1.0, 5.0, 10.0];
    test:assertEquals(obj.floatArrayField, expArray, msg = "expected object field to have initialzed value");
}
