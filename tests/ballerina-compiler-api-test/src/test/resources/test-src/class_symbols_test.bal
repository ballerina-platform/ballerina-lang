// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

class Person1 {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

class Person2 {
    string fname = "John";
    string lname = "Doe";

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

function test() {
    Person1 p1 = new("Pubudu", "Fernando");
    Person2 p2 = new;
    p2 = new Person2();
}

distinct class DistinctPerson {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;
}
