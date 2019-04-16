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

const EXPECTED_OBJECT_FAILURE_MESSAGE = "expected object's ";

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
type NormalObject object {
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
        self.privateMethodDefn(argOne, argTwo);
        return self.defaultVisibilityFloatField;
    }
};

public function NormalObject.publicMethodDecl(string argOne, int argTwo) returns float {
    self.publicStringField = argOne;
    self.privateMethodDecl(argOne, argTwo);
    return self.defaultVisibilityFloatField;
}

private function NormalObject.privateMethodDecl(string argOne, int argTwo) {
    self.defaultVisibilityFloatField += argTwo;
}

function NormalObject.defaultVisibiltyMethodDecl(string argOne, int argTwo) returns float {
    return self.defaultVisibilityFloatField + argTwo;
}

@test:Config {}
function testObjectDeclaration() {
    NormalObject normalObject = new("default string value", 12, 100.0);

    test:assertTrue(normalObject.publicStringField == "default string value",
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "public field to be accessible");

    test:assertTrue(normalObject.getPrivateField() == 12,
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "private field to be accessible via an object method");

    test:assertTrue(normalObject.defaultVisibilityFloatField == 100.0,
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "default visibility field to be accessible");

    test:assertTrue(normalObject.defaultVisibiltyMethodDecl("argOne", 50) == 150.0,
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "default visibility method to be accessible");

    test:assertTrue(normalObject.publicMethodDefn("argOne", 100, 50.0) == 250.0,
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "public method to be accessible");

    test:assertTrue(normalObject.publicMethodDecl("changed string", 100) == 350.0,
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "public method to be accessible");

    test:assertTrue(normalObject.publicStringField == "changed string",
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "public field to be accessible");

    test:assertTrue(normalObject.defaultVisibiltyMethodDefn("argOne", 50) == 400.0,
        msg = EXPECTED_OBJECT_FAILURE_MESSAGE + "default visibility method to be accessible");
}
