 // Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
 //
 // WSO2 LLC. licenses this file to you under the Apache License,
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

function f1() {
    xml A = xml `xml string`;
    [string, int|xml, string...] C = ["text1", 1, A.toString()];
    json jsonTest = <json[]>C;
}

function testTupleToJSONCastRuntimeError() returns error? {
    var e = trap f1();
    return e is error ? e : ();
}

type Teacher record {
    readonly string name;
    readonly int age;
    string school;
};

type Customer record {
    readonly int id;
    readonly string name;
    string lname;
};

type CustomerTable table<Customer|Teacher>;

CustomerTable tab3 = table key(name) [
    {id: 13, name: "Foo", lname: "QWER"},
    {id: 13, name: "Bar", lname: "UYOR"}
];

type Customer2 record {|
    int id;
    string name;
    string lname;
|};

type CustomerEmptyKeyedTbl table<Customer2> key();

function f2() {
    CustomerEmptyKeyedTbl tbl1 = <CustomerEmptyKeyedTbl>tab3;
}

function testCastingWithEmptyKeyedKeylessTbl() returns error? {
    var e = trap f2();
    return e is error ? e : ();
}

type Student record {
    int index;
    int age;
};

type Person record {
    string name;
    int age;
    string address;
};

function f3() returns map<Student> {
    map<Person> testPMap = {};
    map<Student> testSMap = <map<Student>>testPMap;
    return testSMap;
}

function testMapCastingRuntimeError() returns error? {
    var e = trap f3();
    return e is error ? e : ();
}

function f4() {
    string[]|int val1 = [];
    byte[] a = <byte[]> val1;
}

function testListCastingRuntimeError() returns error? {
    var e = trap f4();
    return e is error ? e : ();
}

public class person01 {

    public int age = 0;
    public string name = "";
    public string address = "";

}

public class employee01 {

    public int age = 0;
    public string name = "";
    public string zipcode = "95134";

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

function f5() returns string {
    employee01 e = new (14, "rat");
    person01 p = <person01> e;
    return p.name;
}

function testCastingObjects() returns error? {
    var e = trap f5();
    return e is error ? e : ();
}

public class employee08 {

    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";


    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }

    public function getSSN() returns string {
        return self.ssn;
    }
}

public class person08 {

    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";


    public function getAge() returns int {
        return self.age;
    }

    public function getName() returns string {
        return self.name;
    }

    public function setSSN(string s) {
        self.ssn = s;
    }
}

function f6() returns string {
    employee08 e = new (14, "rat");
    person08 p = <person08> e;
    return p.name;
}

function testCastingObjects2() returns error? {
    var e = trap f6();
    return e is error ? e : ();
}

public class person09 {

    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";


    public function getAge() returns int {
        return self.age;
    }

    public function getName() returns string {
        return self.name;
    }

    public function setSSN(string s) {
        self.ssn = s;
    }
}

public class employee09 {

    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";


    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getAge(int i) returns int {
        return self.age;
    }

    public function getSSN() returns string {
        return self.ssn;
    }
}

function f7() returns string {
    employee09 e = new (14, "rat");
    person09 p = <person09> e;
    return p.name;
}

function testCastingObjects3() returns error? {
    var e = trap f7();
    return e is error ? e : ();
}
