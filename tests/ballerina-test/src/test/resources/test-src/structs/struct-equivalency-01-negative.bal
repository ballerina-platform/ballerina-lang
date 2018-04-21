
public type person01 {
    int age;
    string name;
    string address;
};

public type employee01 {
    int age;
    string name;
    string zipcode = "95134";
};

// Field name mismatch
function testEqOfStructsInSamePackage01() returns (string) {
    employee01 e = {age:14, name:"rat"};
    person01 p = <person01> e;
    return p.name;
}

public type person02 {
    int age;
    string name;
    string address;
};

public type employee02 {
    int age;
    string name;
    int address;
};

// Type name mismatch
function testEqOfStructsInSamePackage02() returns (string) {
    employee02 e = {age:14, name:"rat"};
    person02 p = <person02> e;
    return p.name;
}

public type person03 {
    int age;
    string name;
    string address;
};

public type employee03 {
    int age;
    string name;
};

// Field count mismatch
function testEqOfStructsInSamePackage03() returns (string) {
    employee03 e = {age:14, name:"rat"};
    person03 p = <person03> e;
    return p.name;
}

type person06 {
    int age;
    string name;
    int address;
    string id;
};

type employee06 {
    int age;
    string name;
    string address;
    string id;
    string ssn;
};

// Type mismatch
function testEqOfStructsInSamePackage06() returns (string) {
    employee06 e = {age:14, name:"rat"};
    person06 p = <person06> e;
    return p.name;
}

