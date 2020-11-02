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

public class Student {
    public string name = "";
    public string school = "";
    public int age = 0;

    public function init(string name, string school, int age) {
        self.name = name;
        self.age = age;
        self.school = school;
    }

    public function toString() returns string {
        return "Student{" + self.name + ", " + self.age.toString() + ", " + self.school + "}";
    }

    public function getSchool() returns string {
        return self.school;
    }
}

public class ModuleLevelSubtypableObj {
    public string name = "";
    int age = 0;
}

public class ModuleLevelSubtypableObj2 {
    public string name = "";
    public int age = 0;

    function updateAge(int age) {
        self.age = age;
    }
}

public client class ClientObjectWithoutRemoteMethod {
    public string name;
    public string id = "";

    public function init(string name) {
        self.name = name;
    }
    public function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}

public class NonClientObject {
    public string name;
    public string id = "";

    public function init(string name) {
        self.name = name;
    }
    public function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}
