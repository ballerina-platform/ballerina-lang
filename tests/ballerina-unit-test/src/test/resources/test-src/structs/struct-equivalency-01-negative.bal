
public type person01 record {
    int age = 0;
    string name = "";
    string address = "";
};

public type employee01 record {
    int age = 0;
    string name = "";
    string zipcode = "95134";
};

// Field name mismatch
function testEqOfStructsInSamePackage01() returns (string) {
    employee01 e = {age:14, name:"rat"};
    person01 p = <person01> e;
    return p.name;
}

public type person02 record {
    int age = 0;
    string name = "";
    string address = "";
};

public type employee02 record {
    int age = 0;
    string name = "";
    int address = 0;
};

// Type name mismatch
function testEqOfStructsInSamePackage02() returns (string) {
    employee02 e = {age:14, name:"rat"};
    person02 p = <person02> e;
    return p.name;
}

public type person03 record {
    int age = 0;
    string name = "";
    string address = "";
};

public type employee03 record {
    int age = 0;
    string name = "";
};

// Field count mismatch
function testEqOfStructsInSamePackage03() returns (string) {
    employee03 e = {age:14, name:"rat"};
    person03 p = <person03> e;
    return p.name;
}

type person06 record {
    int age = 0;
    string name = "";
    int address = 0;
    string id = "";
};

type employee06 record {
    int age = 0;
    string name = "";
    string address = "";
    string id = "";
    string ssn = "";
};

// Type mismatch
function testEqOfStructsInSamePackage06() returns (string) {
    employee06 e = {age:14, name:"rat"};
    person06 p = <person06> e;
    return p.name;
}

