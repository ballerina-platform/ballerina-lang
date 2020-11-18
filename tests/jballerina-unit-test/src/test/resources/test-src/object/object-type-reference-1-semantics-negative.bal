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

type Person1 object {
    public int age;
    public string name;
};

type Employee1 record {
    float salary = 0.0;
};

class Manager1 {
    *Person1;

    string dpt = "HR";

    // refering a non-object
    *Employee1;
}

type EmployeeWithSalary object {
    public float salary;
};

type AnotherEmployeeWithSalary object {
    public int salary;
};

class ManagerWithTwoSalaries {
    *Person1;

    string dpt = "HR";
    *EmployeeWithSalary;
    *AnotherEmployeeWithSalary;
}

// Direct circular reference
type Foo object {
    *Foo;
};

// Indirect circular references
type A object {
    *B;
};

type B object {
    *C;
};

type C object {
    *D;
    *E;
};

type D object {
    *A;
};

type E object {
    *C;
};

// Test errors for unimplemented methods
type Person2 object {
    public int age;
    public string name;

    // Unimplemented function at the nested referenced type.
    public function getName(string? title) returns string;
};

type Employee2 object {
    *Person2;
    public float salary;

    // Unimplemented function at the referenced type.
    public function getSalary() returns float;
};

class Manager2 {
    string dpt = "HR";
    *Employee2;
}

class P {
    *Q;
}

type Q record {
    int x = 0;
    string y = "";
};

class R {
    *Person1;
    *Person1;
}

type ObjectWithFunction object {
    public function getName(string? title) returns string;
};

type ObjectWithRedeclaredFunction_1 object {
    *ObjectWithFunction;
    public function getName(string? title) returns string;
};

type ObjectWithRedeclaredFunction_2 object {
    *ObjectWithFunction;
    *ObjectWithRedeclaredFunction_1;
};

class Bar {
    *Baz;   // non existing type
}

class Too {
    *Tar; // non existing type

    string s; // with member

    public function init(string s) {
        self.s = s;
    }
}

type ObjWithSameFunc1 object {
    function abc();
    function xyz(int i);
};

type ObjWithSameFunc2 object {
    function xyz(int i);
    function def(string s) returns string;
};

class ObjWithRedeclaredFuncsViaRefs {
    *ObjWithSameFunc1;
    *ObjWithSameFunc2;

    function xyz(int i) {

    }

    function abc() {

    }

    function def(string s) returns string {
        return s;
    }
}
