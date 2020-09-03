// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Person1 object {
    public int age;
    public string name;

    public function getName(string greeting = "Hi") returns string;
};

public type Employee1 object {
    public float salary;

    public function getBonus(float ratio, int months=12) returns float;
};

public class Manager1 {
    *Person1;

    public string dpt = "HR";

    *Employee1;

    public function getBonus(float ratio, int months=8) returns float {
        return self.salary*ratio*months;
    }

    public function init(int age=20) {
        self.age = age;
        self.name = "John";
        self.salary = 1000.0;
    }

    public function getName(string greeting = "Hello") returns string {
        return greeting + " " + self.name;
    }

}

public type Employee2 object {
    public float salary;
    *Person1;
    public function getBonus(float ratio, int months=12) returns float;
};

public class CorronifiedEmployee {

    *Manager1;

    public boolean  workingFromHome;
    public float    workingFromHomeAllowance;

    public function init(boolean workingFromHome, float salary, float workingFromHomeAllowance, int age, string name) {
        self.age                        = age;
        self.name                       = name;
        self.workingFromHome            = workingFromHome;
        self.salary                     = salary;
        self.workingFromHomeAllowance   = workingFromHomeAllowance;
        self.dpt                        = "Engineering";
    }

    public function Age() returns int {
        return self.age;
    }

    public function setWorkingFromHomeAllowance(float allowance) returns float|() {
        self.workingFromHomeAllowance = allowance;
        if (self.workingFromHome) {
            return self.workingFromHomeAllowance;
        }
        return ();
    }

    public function setWorkingFromHome(boolean workingFromHome) {
        self.workingFromHome = workingFromHome;
    }

    public function getBonus(float ratio, int months=12) returns float {
        if (self.workingFromHome) {
            return self.salary * ratio * months + self.workingFromHomeAllowance;
        }
        return self.salary * ratio * months;
    }

    public function getName(string greeting = "Contactless hello!") returns string {
        return greeting + " " + self.name;
    }
}

public class NormalPerson {
    string name = "John";
}

public type Employee3 object {
    public int|float salary;

    public function getBonus(float ratio, int months=12) returns float;
};
