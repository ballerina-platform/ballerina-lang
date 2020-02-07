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

type Person1 abstract object {
    public int age;
    public string name;

    public function getName() returns string;
};

type Employee1 abstract object {
    public float salary;

    public function getSalary() returns float; 
};

type Manager1 object {
    *Person1;

    string dpt = "HR";

    *Employee1;

    public function getName() returns string {
        return self.name + " from inner function";
    }
    
    function __init() {
        self.age = 99;
        self.name = "sample name 2";
        self.salary = 8.0;
    }

    public function getSalary() returns float {
        return self.salary;
    }
};

public function testSimpleObjectTypeReference() returns [int, string, float, string] {
    Manager1 mgr = new Manager1();
    return [mgr.age, mgr.getName(), mgr.getSalary(), mgr.dpt];
}

type Manager2 object {
    *Person1;

    string dpt = "HR";

    *Employee1;

    public function getName() returns string {
        return self.name + " from inner function";
    }

    function __init(int age=20) {
        self.age = age;
        self.name = "John";
        self.salary = 1000.0;
    }

    public function getSalary() returns float {
        return self.salary;
    }
};

public function testInitTypeReferenceObjectWithNew() returns [int, string, float, string] {
    Manager2 mgr = new Manager2();
    return [mgr.age, mgr.getName(), mgr.getSalary(), mgr.dpt];
}

type Manager3 object {
    string dpt = "HR";

    *Employee2;

    function __init(int age=20) {
        self.age = age;
        self.salary = 2500.0;
        self.name = "Doe";
    }

    public function getName() returns string {
        return self.name + " from outer function";
    }

    public function getSalary() returns float {
        return self.salary;
    }
};

type Employee2 abstract object {
    public float salary;
    *Person1;

    public function getSalary() returns float; 
};

public function testObjectWithChainedTypeReferences() returns [int, string, float, string] {
    Manager3 mgr = new Manager3();
    mgr.name = "John";
    return [mgr.age, mgr.getName(), mgr.getSalary(), mgr.dpt];
}

// Test invoking object member method with default values
type Manager4 object {
    string dpt = "HR";

    *Employee3;

    function __init(string name, int age=25) {
        self.name = name;
        self.age = age;
        self.salary = 3000.0;
    }

    public function getBonus(float ratio, int months=6) returns float {
        return self.salary*ratio*months;
    }

    public function getName(string greeting = "Hello") returns string {
        return greeting + " " + self.name;
    }
};

type Employee3 abstract object {
    public float salary;
    *Person3;

    public function getBonus(float ratio, int months=12) returns float;
};

type Person3 abstract object {
    public int age;
    public string name;

    public function getName(string greeting = "Hi") returns string;
};

public function testAbstractObjectFuncWithDefaultVal() returns [string, float] {
    Manager4 mgr = new Manager4("Jane");
    return [mgr.getName(), mgr.getBonus(0.1)];
}
