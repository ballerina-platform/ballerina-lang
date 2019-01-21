import eq;
import eq2;
import req;
import req2;

public type person1 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    !...;
};

public type employee1 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    !...;
};

function testEquivalenceOfPrivateStructsInSamePackage () returns (string) {
    employee1 e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    person1 p = e;

    return p.ssn;
}

public type person2 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    !...;
};

public type employee2 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    !...;
};

function testEquivalenceOfPublicStructsInSamePackage () returns (string) {
    employee2 e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    person2 p = e;

    return p.ssn;
}


function testEqOfPublicStructs () returns (string) {
    eq:closedEmployee e = {age:14, name:"rat"};
    e.ssn = "234-56-7890:employee";

    eq:closedPerson p = eq:closedPerson.convert(e);

    return p.ssn;
}


public type employee3 record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    !...;
};

function testEqOfPublicStructs1 () returns (string) {
    employee3 e = {age:14, name:"rat"};
    e.ssn = "234-56-1234:employee";

    eq:closedPerson p = e;

    return p.ssn;
}

function testEqOfPublicStructs2 () returns (string) {
    eq2:closedEmployee e = {age:14, name:"rat"};
    e.ssn = "234-56-3345:employee";

    eq:closedPerson p = e;

    return p.ssn;
}




type userA record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "";
    !...;
};

type userB record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "";
    !...;
};

type userFoo record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
    !...;
};


function testRuntimeEqPrivateStructsInSamePackage () returns (string|error) {
    userFoo uFoo = {age:10, name:"ttt", address:"102 Skyhigh street #129, San Jose"};

    userA uA = uFoo;

    userB uB = uA;
    return uB.name;
}


public type userPA record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "";
    !...;
};

public type userPB record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "";
    !...;
};


public type userPFoo record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
    !...;
};

function testRuntimeEqPublicStructsInSamePackage () returns (string|error) {
    userPFoo uFoo = {age:10, name:"Skyhigh", address:"102 Skyhigh street #129, San Jose"};

    userPA uA = uFoo;

    var uB = userPB.convert(uA);
    return uB.name;
}

function testRuntimeEqPublicStructs () returns (string|error) {
    req:closedUserPFoo uFoo = {age:10, name:"Skytop", address:"102 Skyhigh street #129, San Jose"};

    userPA uA = uFoo;

    userPB uB  = uA;
    return uB.name;
}

function testRuntimeEqPublicStructs1 () returns (string|error) {
    req:closedUserPFoo uFoo = {age:10, name:"Brandon", address:"102 Skyhigh street #129, San Jose"};

    // This is a safe cast
    userPA uA = uFoo;

    // This is a unsafe cast
    var uB  = req2:closedUserPB.convert(uA);
    return uB.name;
}

type Foo record {
    string a = "";
    string b = "";
    string c = "";
    int d = 0;
    float e = 0.0;
    person1? p = ();
    !...;
};

type AnotherFoo record {
    string c = "";
    string b = "";
    string a = "";
    int d = 0;
    float e = 0.0;
    person1? p = ();
    !...;
};

function testRecordEquivalence() returns Foo {
    AnotherFoo af = {a: "A", b: "B", c: "C", d: 10};
    Foo f = af;
    return f;
}

function testUnorderedFieldRecordsInAMatch() returns Foo? {
    AnotherFoo|string aFoo = {a: "A", b: "B", c: "C", d: 10};

    if aFoo is AnotherFoo {
        return aFoo;
    }
    return ();
}
