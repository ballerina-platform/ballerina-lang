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

import testorg/subtyping;

type ObjWithPvtField object {
    string name;
    private int ssn;
};

type ObjWithPvtMethod object {
    string name;
    int ssn;

    private function test();
};

class AnotherObjWithAPvtField {
    string name = "";
    private int ssn = 0;
}

class AnotherObjWithPvtMethod {
    string name = "";
    int ssn = 0;

    private function test() {
    }
}

function testObjsWithPvtMembers() {
    ObjWithPvtField o1 = new AnotherObjWithAPvtField();
    ObjWithPvtMethod o2 = new AnotherObjWithPvtMethod();
}

class Subtype1 {
    public string name = "";
    public string address = "";
    int age = 0;
}

class Subtype2 {
    public string name = "";
    public int age = 0;

    function updateAge(int age) {
        self.age = age;
    }
}

function testModuleLevelSubtypableObjs() {
    subtyping:ModuleLevelSubtypableObj o1 = new Subtype1();
    subtyping:ModuleLevelSubtypableObj2 o2 = new Subtype2();
}
