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

// Field name mismatch
function testEqOfObjectsInSamePackage01() returns (string) {
    employee01 e = new (14, "rat");
    person01 p = <person01> e;
    return p.name;
}

public class person02 {

    public int age = 0;
    public string name = "";
    public string address = "";

}

public class employee02 {

    public int age = 0;
    public string name = "";
    public int address = 0;

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

// Type name mismatch
function testEqOfObjectsInSamePackage02() returns (string) {
    employee02 e = new (14, "rat");
    person02 p = <person02> e;
    return p.name;
}

public class person03 {

    public int age = 0;
    public string name = "";
    public string address = "";

}

public class employee03 {

    public int age = 0;
    public string name = "";

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

// Field count mismatch
function testEqOfObjectsInSamePackage03() returns (string) {
    employee03 e = new (14, "rat");
    person03 p = <person03> e;
    return p.name;
}

public class person04 {

    public int age = 0;
    public string name = "";
    public string address = "";

}

public class employee04 {

    public int age = 0;
    public string name = "";
    public string address = "";


    private int id = 0;
    private string ss = "";

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

// Private fields in RHS object name mismatch
function testEqOfObjectsInSamePackage04() returns (string) {
    employee04 e = new (14, "rat");
    person04 p = <person04> e;
    return p.name;
}


public class person05 {

    public int age = 0;
    public string name = "";


    private string address = "";
    private string id = "";

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

public class employee05 {

    public int age = 0;
    public string name = "";
    public string address = "";
    public string id = "";
    public string ssn = "";

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

// Private fields in LHS object name mismatch
function testEqOfObjectsInSamePackage05() returns (string) {
    employee05 e = new (14, "rat");
    person05 p = <person05> e;
    return p.name;
}

class person06 {

    public int age = 0;
    public string name = "";
    public int address = 0;
    public string id = "";

}

class employee06 {

    public int age = 0;
    public string name = "";
    public string address = "";
    public string id = "";
    public string ssn = "";

    function init (int age, string name) {
        self.age = age;
        self.name = name;
    }
}

// Private Objects type mismatch
function testEqOfObjectsInSamePackage06() returns (string) {
    employee06 e = new (14, "rat");
    person06 p = <person06> e;
    return p.name;
}


public class person07 {

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

public class employee07 {

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
}

// Public Objects attached function count mismatch
function testEqOfObjectsInSamePackage07() returns (string) {
    employee07 e = new (14, "rat");
    person07 p = <person07> e;
    return p.name;
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

// Public Objects attached function visibility mismatch
function testEqOfObjectsInSamePackage08() returns (string) {
    employee08 e = new (14, "rat");
    person08 p = <person08> e;
    return p.name;
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

// Public Objects attached function signature mismatch
function testEqOfObjectsInSamePackage09() returns (string) {
    employee09 e = new (14, "rat");
    person09 p = <person09> e;
    return p.name;
}


public class PersonInOrder {
    public int age = 0;
    public string name = "";
    public string address = "";

    function init (string name, int age) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns (string) {
        return self.name;
    }

    public function getAge() returns (int) {
        return self.age;
    }

    public function getAddress() returns (string) {
        return self.address;
    }
}

public class PersonNotInOrder {

    public function getName() returns (string) {
        return self.name;
    }

    // This is not in order
    public string name = "";

    public function getAge() returns (int) {
        return self.age;
    }

    function init (string name, int age) {
        self.age = age;
        self.name = name;
    }

    public int age = 0;

    public function getAddress() returns (string) {
        return self.address;
    }

    public string address = "";
}

function testObjectMemberOrder() returns [PersonInOrder, PersonNotInOrder] {
    PersonInOrder p1 = new("John", 35);
    PersonNotInOrder p2 = p1;

    PersonNotInOrder p3 = new ("Doe", 45);
    PersonInOrder p4 = p3;

    return [p4, p2];
}

client class ObjWithOnlyRemoteMethod {
    public string name;
    public string id = "";

    function init(string name) {
        self.name = name;
    }
    public remote function send(string message) returns error? {
    }
    public remote function receive(string message) {
    }
}

client class ObjWithRemoteMethod {
    public string name;
    public string id = "";

    function init(string name) {
        self.name = name;
    }
    public remote function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}

class NonClientObj {
    public string name;
    public string id = "";

    function init(string name) {
        self.name = name;
    }
    public function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}

client class ClientObjWithoutRemoteMethod {
    public string name;
    public string id = "";

    function init(string name) {
        self.name = name;
    }
    public function send(string message) returns error? {
    }
    public function receive(string message) {
    }
}

function testAssignabilityOfObjectsWithAndWithoutRemoteMethods1() {
    NonClientObj e = new ("email-1");
    ObjWithRemoteMethod p = e;
}

function testAssignabilityOfObjectsWithAndWithoutRemoteMethods2() {
    ClientObjWithoutRemoteMethod e = new ("email-2");
    ObjWithRemoteMethod p = e;
}

function testAssignabilityOfObjectsWithAndWithoutRemoteMethods3() {
    ClientObjWithoutRemoteMethod e = new ("email-3");
    ObjWithOnlyRemoteMethod p = e;
}

function testAssignabilityOfObjectsWithAndWithoutRemoteMethods4() {
    NonClientObj e = new ("email-4");
    ObjWithOnlyRemoteMethod p = e;
}

function testAssignabilityOfObjectsWithAndWithoutRemoteMethods5() {
    ObjWithRemoteMethod e = new ("email-4");
    NonClientObj p = e;
}
