public struct person {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    private:
        string ssn;
        int id;
}

public function <person p> getName() returns (string) {
    return p.name;
}

public function <person p> getAge() returns (int) {
    return p.age;
}

function <person p> getSSN() returns (string) {
    return p.ssn;
}

function <person p> setSSN(string ssn) {
    p.ssn = ssn;
}

public struct user {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    private:
        int id;
}

public function <user u> getName() returns (string) {
    return u.name;
}

public function <user u> getAge() returns (int) {
    return u.age;
}

function <user u> getSSN() returns (string) {
    return u.ssn;
}

function <user u> setSSN(string ssn) {
    u.ssn = ssn;
}                           

public struct employee {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    private:
        string ssn;
        int id;
        int employeeId = 123456;
}

public function <employee e> getName() returns (string) {
    return e.name;
}

function <employee e> getAge() returns (int) {
    return e.age;
}

function <employee e> getSSN() returns (string) {
    return e.ssn;
}

function <employee e> setSSN(string ssn) {
    e.ssn = ssn;
}

public function <employee e> getEmployeeId() returns (int) {
    return e.employeeId;
}

function testEquivalenceOfStructsInSamePackage1() returns (string) {
    user u = {age:14, name:"rat"};
    u.setSSN("234-56-7890");

    person p = (person) u;

    return p.getSSN();
}

function testEquivalenceOfStructsInSamePackage2() returns (string) {
    employee e = {age:14, name:"rat"};
    e.setSSN("234-56-7890");

    person p = (person) e;

    return p.getSSN();
}
