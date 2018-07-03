import eq;
import eq2;
import req;
import req2;

public type person1 object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "95134";
    public string ssn;
    public int id;

    public new () {}

    public function getName () returns (string);

    public function getAge () returns (int);

    public function getSSN () returns (string);

    public function setSSN (string s);
};

function person1::getName () returns (string) {
    return self.name;
}

function person1::getAge () returns (int) {
    return self.age;
}

function person1::getSSN () returns (string) {
    return self.ssn;
}

function person1::setSSN (string s) {
    self.ssn = s;
}

public type employee1 object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "95134";
    public string ssn;
    public int id;
    public int employeeId = 123456;

    public new (age, name){}

    public function getName () returns (string);

    public function getAge () returns (int);

    public function getSSN () returns (string);

    public function setSSN (string s);

    public function getEmployeeId () returns (int);
};

function employee1::getName () returns (string) {
    return self.name;
}

function employee1::getAge () returns (int) {
    return self.age;
}

function employee1::getSSN () returns (string) {
    return self.ssn + ":employee";
}

function employee1::setSSN (string s) {
    self.ssn = s;
}

function employee1::getEmployeeId () returns (int) {
    return self.employeeId;
}


function testEquivalenceOfPrivateStructsInSamePackage () returns (string) {
    employee1 e = new (14, "rat");
    e.setSSN("234-56-7890");

    person1 p = <person1>e;

    return p.getSSN();
}

public type person2 object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "95134";
    public string ssn;
    public int id;

    public function getName () returns (string);

    public function getAge () returns (int);

    public function getSSN () returns (string);

    public function setSSN (string s);
};

function person2::getName () returns (string) {
    return self.name;
}

function person2::getAge () returns (int) {
    return self.age;
}

function person2::getSSN () returns (string) {
    return self.ssn;
}

function person2::setSSN (string s) {
    self.ssn = s;
}

public type employee2 object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "95134";
    public string ssn;
    public int id;
    public int employeeId = 123456;

    public function getName () returns (string);

    public function getAge () returns (int);

    public function getSSN () returns (string);

    public function setSSN (string s);

    public function getEmployeeId () returns (int);
};

function employee2::getName () returns (string) {
    return self.name;
}

function employee2::getAge () returns (int) {
    return self.age;
}

function employee2::getSSN () returns (string) {
    return self.ssn + ":employee";
}

function employee2::setSSN (string s) {
    self.ssn = s;
}

function employee2::getEmployeeId () returns (int) {
    return self.employeeId;
}

function testEquivalenceOfPublicStructsInSamePackage () returns (string) {
    employee2 e = new;
    e.age = 14;
    e.name = "rat";
    e.setSSN("234-56-7890");

    person2 p = <person2>e;

    return p.getSSN();
}


function testEqOfPublicStructs () returns (string) {
    eq:employee e = new (14, "rat");
    e.setSSN("234-56-7890");

    eq:person p = <eq:person>e;

    return p.getSSN();
}


public type employee3 object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "95134";
    public string ssn;
    public int id;
    public int employeeId = 123456;

    public function getName () returns (string);

    public function getAge () returns (int);

    public function getSSN () returns (string);

    public function setSSN (string s);

    public function getEmployeeId () returns (int);
};

function employee3::getName () returns (string) {
    return self.name;
}

function employee3::getAge () returns (int) {
    return self.age;
}

function employee3::getSSN () returns (string) {
    return self.ssn + ":employee";
}

function employee3::setSSN (string s) {
    self.ssn = s;
}

function employee3::getEmployeeId () returns (int) {
    return self.employeeId;
}

function testEqOfPublicStructs1 () returns (string) {
    employee3 e = new;
    e.age = 14;
    e.name = "rat";
    e.setSSN("234-56-1234");

    eq:person p = <eq:person>e;

    return p.getSSN();
}

function testEqOfPublicStructs2 () returns (string) {
    eq2:employee e = new;
    e.age = 14;
    e.name = "rat";
    e.setSSN("234-56-3345");

    eq:person p = <eq:person>e;

    return p.getSSN();
}




type userA object {
    public int age;
    public string name;

    function getName () returns (string);

    function getAge () returns (int);
};

function userA::getName () returns (string) {
    return self.name;
}

function userA::getAge () returns (int) {
    return self.age;
}

type userB object {
    public int age;
    public string name;
    public string address;

    function getName () returns (string);

    function getAge () returns (int);
};

function userB::getName () returns (string) {
    return self.name;
}

function userB::getAge () returns (int) {
    return self.age;
}

type userFoo object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "23468";

    function getName () returns (string);

    function getAge () returns (int);
};

function userFoo::getName () returns (string) {
    return self.name;
}

function userFoo::getAge () returns (int) {
    return self.age;
}


function testRuntimeEqPrivateStructsInSamePackage () returns (string) {
    userFoo uFoo = new;
    uFoo.age = 10;
    uFoo.name = "ttt";
    uFoo.address = "102 Skyhigh street #129, San Jose";

    // This is a safe cast
    var uA = <userA>uFoo;

    // This is a unsafe cast
    var uB =check <userB>uA;
    return uB.name;
}


public type userPA object {
    public int age;
    public string name;

    public function getName () returns (string);

    public function getAge () returns (int);
};

function userPA::getName () returns (string) {
    return self.name;
}

function userPA::getAge () returns (int) {
    return self.age;
}

public type userPB object {
    public int age;
    public string name;
    public string address;

    public function getName () returns (string);

    public function getAge () returns (int);
};

function userPB::getName () returns (string) {
    return self.name;
}

function userPB::getAge () returns (int) {
    return self.age;
}

public type userPFoo object {
    public int age;
    public string name;
    public string address;
    public string zipcode = "23468";

    public function getName () returns (string);

    public function getAge () returns (int);
};

function userPFoo::getName () returns (string) {
    return self.name;
}

function userPFoo::getAge () returns (int) {
    return self.age;
}


function testRuntimeEqPublicStructsInSamePackage () returns (string) {
    userPFoo uFoo = new;
    uFoo.age = 10;
    uFoo.name = "Skyhigh";
    uFoo.address = "102 Skyhigh street #129, San Jose";

    // This is a safe cast
    var uA = <userPA>uFoo;

    // This is a unsafe cast
    var uB = <userPB>uA;
    match uB {
        error err => return err.message;
        userPB user=> return user.name;
    }
}

function testRuntimeEqPublicStructs () returns (string) {
    req:userPFoo uFoo = new (10, "Skytop", "102 Skyhigh street #129, San Jose");

    // This is a safe cast
    var uA = <userPA>uFoo;

    // This is a unsafe cast
    var uB  = <userPB>uA;
    match uB {
        error err => return err.message;
        userPB user=> return user.name;
    }
}

function testRuntimeEqPublicStructs1 () returns (string) {
    req:userPFoo uFoo = new (10, "Brandon", "102 Skyhigh street #129, San Jose");

    // This is a safe cast
    var uA = <userPA>uFoo;

    // This is a unsafe cast
    var uB  = <req2:userPB>uA;
    match uB {
        error err => return err.message;
        req2:userPB user=> return user.getName();
    }
}

type personC object {
    public string name;
    public addressStruct address;

    function setContact(addressStruct ad);

    function getAddress() returns (string);
};

function personC::setContact(addressStruct ad){
    self.address = ad;
}

function personC::getAddress() returns (string){
    return self.address.toString();
}

type addressStruct object {
    public int no;
    public string city;

    function toString() returns (string);
};

function addressStruct::toString() returns (string){
    return self.no + self.city;
}

type officeAddressStruct object {
    public int no;
    public string city;
    public string department;

    function toString() returns (string);
};

function officeAddressStruct::toString() returns (string){
    return self.department + self.no + self.city;
}

function testStructEquivalencyWithArguments() returns (string, string, string){
    personC p = new;
    p.name = "tom";
    addressStruct a = new;
    a.no = 1;
    a.city = "CMB";
    officeAddressStruct o = new;
    o.no = 2;
    o.city = "CMB";
    o.department = "ENG";
    // testing assignment.
    addressStruct b = o;
    string result1 = b.toString();

    p.setContact(a);
    string result2 = p.getAddress();

    // testing value passing.
    p.setContact(o);
    string result3 = p.getAddress();
    return (result1, result2, result3);
}

function testStructEquivalencyWithFunctionType () returns (string, string) {
    string s1;
    string s2;
    SomeOtherStruct x = new;
    x.s = "sss";
    AnyStruct aa = new;
    s1 = aa.shout(x);
    _ = aa.call();

    SomeStruct ss = new;
    ss.s = "s";
    AnyStruct aaa = ss;
    s2 = aaa.shout(x);
    _ = aaa.call();
    return (s1,s2);
}

type AnyStruct object {
    function shout (AnotherAnyStruct aa) returns (string);

    function call () returns (AnotherAnyStruct);
};

function AnyStruct::shout (AnotherAnyStruct aa) returns (string) {
    var j =check <json>aa;
    return "anyStruct" + j.toString();
}

function AnyStruct::call () returns (AnotherAnyStruct) {
    return new AnotherAnyStruct() ;
}

type SomeStruct object {
    public string s;

    function shout (SomeOtherStruct aa) returns (string);

    function call () returns (SomeOtherStruct);
};

function SomeStruct::shout (SomeOtherStruct aa) returns (string) {
    var j = check <json>aa;
    return "someStruct" + (j.toString());
}

function SomeStruct::call () returns (SomeOtherStruct) {
    SomeOtherStruct s = new;
    s.s= "return";
    return s;
}

type SomeOtherStruct object {
    public string s;
};

type AnotherAnyStruct object {
};

type Foo "a" | "b" | "c";

type Person object {
    private string name;

    new(name){}

    function getPerson() returns Person {
        error err = {message: "Unsupported operation"};
        throw err;
    }
};

type Employee object {
    private string name;
    private string id;

    new(name,id){}

    function getPerson() returns Person {
        return self;
    }
};

function testTupleMatchWithObjectEquivalency() returns string {
  future<(Foo, Person) | () | error> f = start getPerson();
    ((Foo, Person) | () | error) res = await f;

    int[] i = [1, 2, 3];

    foreach y in i {
        match res {
            (Foo, Person) x => {
                return "SUCCESS";
            }
            () => { return "ERROR"; }
            error err => {
                return "ERROR";
            }
        }
    }
}

function getPerson() returns (Foo, Person) | () | error {
    Foo f = "b";
    Employee p = new("foo","20");
    return (f,p);
}

function testObjectEqViewFromThirdPackage() returns (string) {
    eq:BarObj barObj = new();
    eq2:FooObj fooObj = check <eq2:FooObj> barObj;
    return fooObj.name;
}
