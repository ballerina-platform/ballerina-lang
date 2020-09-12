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

const EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE = "expected referenced abstract object ";

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

// An abstract object type must not have an object-ctor-function and does
// not have an implicit initial value.

// object-type-reference :=
//   * type-descriptor-reference ;

// The type-descriptor-reference in an object-type-reference must reference a an abstract object type.
// The object-member-descriptors from the referenced type are copied into the type being defined;
// the meaning is the same as if they had been specified explicitly.

// If a non-abstract object type OT has a type reference to an abstract object type AT,
// then each method declared in AT must be defined in OT using either a method-defn or an outside-method-defn.
type AbstractObject object {
    public string publicStringField;
    float defaultVisibilityFloatField;

    function getPrivateField() returns int;
    function defaultVisibiltyMethodDecl(string argOne, int argTwo);
    public function publicMethodDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float;
    function defaultVisibiltyMethodOutsideDecl(string argOne, int argTwo);
    public function publicMethodOutsideDecl(string argOne, int argTwo, float defaultVisibilityFloatField) returns float;
};

type ObjReferenceToAbstractObject object {
    *AbstractObject;
    private int privateIntField;

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

    function init(string argOne, int argTwo, float argThree) {
        self.publicStringField = argOne;
        self.privateIntField = argTwo;
        self.defaultVisibilityFloatField = argThree;
    }
};

function ObjReferenceToAbstractObject.defaultVisibiltyMethodOutsideDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

public function ObjReferenceToAbstractObject.publicMethodOutsideDecl(string argOne, int argTwo,
                                                                     float defaultVisibilityFloatField) returns float {
    self.defaultVisibilityFloatField += defaultVisibilityFloatField + argTwo;
    return self.defaultVisibilityFloatField;
}

@test:Config {}
function testAbstractObjectDeclaration() {
    ObjReferenceToAbstractObject abstractObj = new("string", 12, 100.0);

    test:assertTrue(abstractObj.publicStringField == "string",
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "public field to be accessible");

    test:assertTrue(abstractObj.getPrivateField() == 12,
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "private field to be accessible via object method");

    test:assertTrue(abstractObj.defaultVisibilityFloatField == 100.0,
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "default visibilty field to be accessible");

    test:assertTrue(abstractObj.defaultVisibiltyMethodDecl("argOne", 50) == (),
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "default visibility method to be accessible");

    test:assertTrue(abstractObj.defaultVisibiltyMethodOutsideDecl("argOne", 25) == (),
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "default visibility method declared outside to be accessible");

    test:assertTrue(abstractObj.publicMethodOutsideDecl("argOne", 25, 25.0) == 225.0,
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "public method declared outside to be accessible");

    test:assertTrue(abstractObj.publicMethodDecl("argOne", 125, 25) == 375.0,
        msg = EXPECTED_ABSTRACT_OBJECT_FAILURE_MESSAGE + "public visibility method to be accessible");
}
