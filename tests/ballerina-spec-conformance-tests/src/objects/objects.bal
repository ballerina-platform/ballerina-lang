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

public type PublicNormalObject object {
    public string publicStringField;
    private int counter;

    public function init(string argOne) {
        self.publicStringField = argOne;
        self.counter = 10;
    }

    public function publicMethodDefn() returns int {
        self.counter += 10;
        return self.counter;
    }

    public function publicMethodDecl() returns int;
};

public function PublicNormalObject.publicMethodDecl() returns int {
    self.counter += 10;
    return self.counter;
}

public client class PublicClientObject {
    public string publicStringField;
    private int counter;

    public function init(string argOne) {
        self.publicStringField = argOne;
        self.counter = 10;
    }

    public function publicMethodDefn() returns int {
        self.counter += 10;
        return self.counter;
    }

    public remote function publicRemoteMethodDefn(string argOne) returns int {
        self.publicStringField = argOne;
        self.counter += 10;
        return self.counter;
    }

    public function publicMethodDecl() returns int;
    public remote function publicRemoteMethodDecl() returns int;
}

public function PublicClientObject.publicMethodDecl() returns int {
    self.counter += 10;
    return self.counter;
}

public remote function PublicClientObject.publicRemoteMethodDecl() returns int {
    self.counter += 10;
    return self.counter;
}

type AbstractObject object {
    public string publicStringField;

    public function publicMethodDecl(string argOne) returns int;
    public function publicMethodDeclaredOutside() returns int;
};

public type ObjReferenceToAbstractObject object {
    *AbstractObject;
    private int counter;

    public function getPrivateField() returns int {
        return self.counter;
    }

    public function publicMethodDecl(string argOne) returns int {
        self.publicStringField = argOne;
        self.counter += 10;
        return self.counter;
    }

    public function init(string argOne) {
        self.publicStringField = argOne;
        self.counter = 10;
    }
};

public function ObjReferenceToAbstractObject.publicMethodDeclaredOutside() returns int {
    self.counter += 10;
    return self.counter;
}

public type AbstractClientObject client object {
    public string publicStringField;

    public function publicMethodDecl() returns int;
    public remote function publicRemoteMethodDecl(string argOne) returns int;
    public function publicMethodDeclaredOutside() returns int;
    public remote function publicRemoteMethodDeclaredOutside() returns int;
};

public client class ObjReferenceToAbstractClientObject {
    *AbstractClientObject;
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

public function ObjReferenceToAbstractClientObject.publicMethodDeclaredOutside() returns int {
    self.counter += 10;
    return self.counter;
}

public remote function ObjReferenceToAbstractClientObject.publicRemoteMethodDeclaredOutside() returns int {
    self.counter += 10;
    return self.counter;
}
