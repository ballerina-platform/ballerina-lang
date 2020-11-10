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

const EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE = "expected client object's ";

// Objects are a combination of public and private fields along with a set of associated
// functions, called methods, that can be used to manipulate them. An object’s methods are
// associated with the object when the object is constructed and cannot be changed thereafter.

// object-type-descriptor := object-type-quals object { object-member-descriptor* }
// object-type-quals := [abstract] [client] | [client] abstract
// object-member-descriptor := object-field-descriptor | object-method | object-type-reference

// object-field-descriptor := [visibility-qual] type-descriptor field-name ;

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
client class ClientObject {
    public string publicStringField;
    private int privateIntField;
    float defaultVisibilityFloatField;

    function init(string argOne, int argTwo, float argThree) {
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
}

public function ClientObject.publicMethodDecl(string argOne, int argTwo) returns float {
    self.publicStringField = argOne;
    self.defaultVisibilityFloatField += argTwo;
    return self.defaultVisibilityFloatField;
}

private function ClientObject.privateMethodDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

function ClientObject.defaultVisibiltyMethodDecl(string argOne, int argTwo) returns float {
    self.privateMethodDecl(argOne, argTwo);
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
    self->privateRemoteMethodDecl(argOne, argTwo);
}

@test:Config {}
function testClientObjectDeclaration() {
    ClientObject clientObject = new("default string value", 12, 100.0);

    test:assertEquals(clientObject.publicStringField, "default string value",
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "public field to be accessible");

    test:assertEquals(clientObject.getPrivateField(), 12,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "private field to be accessible via an object method");

    test:assertEquals(clientObject.defaultVisibilityFloatField, 100.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "default visibility field to be accessible");

    test:assertEquals(clientObject.defaultVisibiltyMethodDecl("argOne", 50), 150.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "public visibility method to be accessible");

    _ = clientObject->defaultVisibiltyRemoteMethodDecl("argOne", 50);
    test:assertEquals(clientObject.defaultVisibilityFloatField, 200.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "default visibility field to be accessible");

    _ = clientObject->publicRemoteMethodDecl("argOne", 50);
    test:assertEquals(clientObject.defaultVisibilityFloatField, 250.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "default visibility field to be accessible");

    test:assertEquals(clientObject.publicMethodDefn("argOne", 100, 150), 500.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "public method to be accessible");

    test:assertEquals(clientObject.publicMethodDecl("changed string", 50), 550.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "public method to be accessible");

    test:assertEquals(clientObject.publicStringField, "changed string",
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "public field to be accessible");

    test:assertEquals(clientObject.defaultVisibiltyMethodDefn("argOne", 25), 575.0,
        msg = EXPECTED_CLIENT_OBJECT_FAILURE_MESSAGE + "default visibility method to be accessible");
}
