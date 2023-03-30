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

public type Person record {|
    string name;
    int age;
|};

type Employee record {|
    *Person;
    string designation;
|};

public type BasicType int|float|boolean|string|decimal;

public class PersonObj {
    string name;
    int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    public function getName() returns string => self.name;

    public function getAge() returns int => self.age;
}

public type Digit 0|1|2|3|4|5|6|7|8|9;

public type FileNotFoundError distinct error;

public type EofError distinct error;

public type Error FileNotFoundError|EofError;

public enum Colour {
   RED, GREEN, BLUE
}

public type Pet object {
    string name;

    public function getName() returns string;

    public function kind() returns string;
};

public class Dog {
    *Pet;

    public function init(string name) {
        self.name = name;
    }

    public function getName() returns string => self.name;

    public function kind() returns string => "Dog";
}

public type Student record {|
    *Person;
    string school;
|};

public type Cat object {
    *Pet;
};

public type Annot record {|
    string host;
|};

public class EmployeeObj {
    string name;
    public string address = "No 20, Palm Grove";
    public int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }
}

public type HumanObj readonly & object {
    public int age;
    public isolated function eatFunction() returns string;
    public isolated function walkFunction() returns int;
};

public readonly class Human {
    *HumanObj;

    public isolated function init() {
        self.age = 10;
    }

    public isolated function eatFunction() returns string {
        return "Eating";
    }

    public isolated function walkFunction() returns int {
        return 0;
    }
}

public isolated function loadHuman() returns HumanObj {
    return new Human();
}

public type Detail record {
   int severity;
};

public type ApplicationResponseError error & error<Detail>;

public function testAnonTypeDefSymbolsIsNotVisible() {
    ApplicationResponseError err = error("",  severity = 1);
    Detail _ = err.detail();
}

public type Service distinct service object {
};

public type FnTypeA function(int m, int n, float p) returns string;

public type FnTypeB function(int m, function(int a, string... b) returns string fn2) returns string;
