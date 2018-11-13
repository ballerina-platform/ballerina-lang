import eq;
import eq2;
import req;
import req2;

public type person1 record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
};

public type employee1 record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

function testEquivalenceOfPrivateStructsInSamePackage () returns (string) {
    employee1 e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    person1 p = <person1>e;

    return p.ssn;
}

public type person2 record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
};

public type employee2 record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

function testEquivalenceOfPublicStructsInSamePackage () returns (string) {
    employee2 e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    person2 p = <person2>e;

    return p.ssn;
}


function testEqOfPublicStructs () returns (string) {
    eq:employee e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    eq:person p = <eq:person>e;

    return p.ssn;
}


public type employee3 record {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

function testEqOfPublicStructs1 () returns (string) {
    employee3 e = {age:14, name:"rat"};
    e.ssn = "234-56-1234:employee";

    eq:person p = <eq:person>e;

    return p.ssn;
}

function testEqOfPublicStructs2 () returns (string) {
    eq2:employee e = {age:14, name:"rat"};
    e.ssn = "234-56-3345:employee";

    eq:person p = <eq:person>e;

    return p.ssn;
}




type userA record {
    int age;
    string name;
};

type userB record {
    int age;
    string name;
    string address;
};

type userFoo record {
    int age;
    string name;
    string address;
    string zipcode = "23468";
};


function testRuntimeEqPrivateStructsInSamePackage () returns (string) {
    userFoo uFoo = {age:10, name:"ttt", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    var uA = <userA>uFoo;

    // This is a unsafe cast
    var uB = check <userB>uA;
    return uB.name;
}


public type userPA record {
    int age;
    string name;
};

public type userPB record {
    int age;
    string name;
    string address;
};


public type userPFoo record {
    int age;
    string name;
    string address;
    string zipcode = "23468";
};

function testRuntimeEqPublicStructsInSamePackage () returns (string) {
    userPFoo uFoo = {age:10, name:"Skyhigh", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    var uA = <userPA>uFoo;

    // This is a unsafe cast
    var uB = <userPB>uA;
    match uB {
        error err => return err.reason();
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
        error err => return err.reason();
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
        error err => return err.reason();
        userPB user=> return user.name;
    }
}

type Foo record {
    string a;
    string b;
    string c;
};

type AnotherFoo record {
    string c;
    string b;
    string a;
    int d;
    float e;
    person1? p;
};

function testRecordEquivalence() returns Foo {
    AnotherFoo af = {a: "A", b: "B", c: "C", d: 10};
    af.f = "rest field";
    Foo f = af;
    return f;
}

function testUnorderedFieldRecordsInAMatch() returns Foo? {
    AnotherFoo|string aFoo = {a: "A", b: "B", c: "C", d: 10, f: "rest field"};

    match aFoo {
        Foo f => return f;
        any => return ();
    }
}
