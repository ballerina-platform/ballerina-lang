import eq;
import eq2;
import req;
import req2;

public type person1 {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
};

public function <person1 p>  person1 () {
}

public function <person1 p> getName () returns (string) {
    return p.name;
}

public function <person1 p> getAge () returns (int) {
    return p.age;
}

public function <person1 p> getSSN () returns (string) {
    return p.ssn;
}

public function <person1 p> setSSN (string ssn) {
    p.ssn = ssn;
}

public type employee1 {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

public function <employee1 p>  employee1 () {
}

public function <employee1 e> getName () returns (string) {
    return e.name;
}

public function <employee1 e> getAge () returns (int) {
    return e.age;
}

public function <employee1 e> getSSN () returns (string) {
    return e.ssn + ":employee";
}

public function <employee1 e> setSSN (string ssn) {
    e.ssn = ssn;
}

public function <employee1 e> getEmployeeId () returns (int) {
    return e.employeeId;
}


function testEquivalenceOfPrivateStructsInSamePackage () returns (string) {
    employee1 e = {age:14, name:"rat"};
    e.setSSN("234-56-7890");

    person1 p = <person1>e;

    return p.getSSN();
}

public type person2 {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
};

public function <person2 p> getName () returns (string) {
    return p.name;
}

public function <person2 p> getAge () returns (int) {
    return p.age;
}

public function <person2 p> getSSN () returns (string) {
    return p.ssn;
}

public function <person2 p> setSSN (string ssn) {
    p.ssn = ssn;
}

public type employee2 {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

public function <employee2 e> getName () returns (string) {
    return e.name;
}

public function <employee2 e> getAge () returns (int) {
    return e.age;
}

public function <employee2 e> getSSN () returns (string) {
    return e.ssn + ":employee";
}

public function <employee2 e> setSSN (string ssn) {
    e.ssn = ssn;
}

public function <employee2 e> getEmployeeId () returns (int) {
    return e.employeeId;
}

function testEquivalenceOfPublicStructsInSamePackage () returns (string) {
    employee2 e = {age:14, name:"rat"};
    e.setSSN("234-56-7890");

    person2 p = <person2>e;

    return p.getSSN();
}


function testEqOfPublicStructs () returns (string) {
    eq:employee e = {age:14, name:"rat"};
    e.setSSN("234-56-7890");

    eq:person p = <eq:person>e;

    return p.getSSN();
}


public type employee3 {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

public function <employee3 e> getName () returns (string) {
    return e.name;
}

public function <employee3 e> getAge () returns (int) {
    return e.age;
}

public function <employee3 e> getSSN () returns (string) {
    return e.ssn + ":employee";
}

public function <employee3 e> setSSN (string ssn) {
    e.ssn = ssn;
}

public function <employee3 e> getEmployeeId () returns (int) {
    return e.employeeId;
}

function testEqOfPublicStructs1 () returns (string) {
    employee3 e = {age:14, name:"rat"};
    e.setSSN("234-56-1234");

    eq:person p = <eq:person>e;

    return p.getSSN();
}

function testEqOfPublicStructs2 () returns (string) {
    eq2:employee e = {age:14, name:"rat"};
    e.setSSN("234-56-3345");

    eq:person p = <eq:person>e;

    return p.getSSN();
}




type userA {
    int age;
    string name;
};

function <userA ua> getName () returns (string) {
    return ua.name;
}

function <userA ua> getAge () returns (int) {
    return ua.age;
}

type userB {
    int age;
    string name;
    string address;
};

function <userB ub> getName () returns (string) {
    return ub.name;
}

function <userB ub> getAge () returns (int) {
    return ub.age;
}

type userFoo {
    int age;
    string name;
    string address;
    string zipcode = "23468";
};

function <userFoo u> getName () returns (string) {
    return u.name;
}

function <userFoo u> getAge () returns (int) {
    return u.age;
}


function testRuntimeEqPrivateStructsInSamePackage () returns (string) {
    userFoo uFoo = {age:10, name:"ttt", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    var uA = <userA>uFoo;

    // This is a unsafe cast
    var uB = check <userB>uA;
    return uB.name;
}


public type userPA {
    int age;
    string name;
};

public function <userPA ua> getName () returns (string) {
    return ua.name;
}

public function <userPA ua> getAge () returns (int) {
    return ua.age;
}

public type userPB {
    int age;
    string name;
    string address;
};

public function <userPB ub> getName () returns (string) {
    return ub.name;
}

public function <userPB ub> getAge () returns (int) {
    return ub.age;
}

public type userPFoo {
    int age;
    string name;
    string address;
    string zipcode = "23468";
};

public function <userPFoo u> getName () returns (string) {
    return u.name;
}

public function <userPFoo u> getAge () returns (int) {
    return u.age;
}


function testRuntimeEqPublicStructsInSamePackage () returns (string) {
    userPFoo uFoo = {age:10, name:"Skyhigh", address:"102 Skyhigh street #129, San Jose"};

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
    req:userPFoo uFoo = {age:10, name:"Skytop", address:"102 Skyhigh street #129, San Jose"};

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
    req:userPFoo uFoo = {age:10, name:"Brandon", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    var uA = <userPA>uFoo;

    // This is a unsafe cast
    var uB  = <req2:userPB>uA;
    match uB {
        error err => return err.message;
        userPB user=> return user.getName();
    }
}

type personC {
    string name;
    addressStruct address;
};

function <personC p> setContact(addressStruct ad){
    p.address = ad;
}

function <personC p> getAddress() returns (string){
    return p.address.toString();
}

type addressStruct {
    int no;
    string city;
};

function <addressStruct ad> toString() returns (string){
    return ad.no + ad.city;
}

type officeAddressStruct {
    int no;
    string city;
    string department;
};

function <officeAddressStruct ad> toString() returns (string){
    return ad.department + ad.no + ad.city;
}

function testStructEquivalencyWithArguments() returns (string, string, string){
    personC p = { name : "tom" };
    addressStruct a = { no: 1, city: "CMB"};
    officeAddressStruct o = { no: 2, city: "CMB", department: "ENG"};
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
    SomeOtherStruct x = {s:"sss"};
    AnyStruct aa = {};
    s1 = aa.shout(x);
    _ = aa.call();

    SomeStruct ss = {s:"s"};
    AnyStruct aaa = ss;
    s2 = aaa.shout(x);
    _ = aaa.call();
    return (s1,s2);
}

type AnyStruct {
};

function <AnyStruct a> shout (AnotherAnyStruct aa) returns (string) {
    var j = check <json>aa;
    return "anyStruct" + (j.toString() but { () => ""});
}

function <AnyStruct a> call () returns (AnotherAnyStruct) {
    return {} ;
}

type SomeStruct {
    string s;
};

function <SomeStruct b> shout (SomeOtherStruct aa) returns (string) {
    var j = check <json>aa;
    return "someStruct" + (j.toString() but { () => ""});
}

function <SomeStruct b> call () returns (SomeOtherStruct) {
    return { s : "return"};
}

type SomeOtherStruct {
    string s;
};

type AnotherAnyStruct {
};
