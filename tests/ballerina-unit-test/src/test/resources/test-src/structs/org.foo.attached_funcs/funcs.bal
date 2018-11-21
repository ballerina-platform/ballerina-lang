

public type user record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
};

public function <user u> getName() returns (string) {
    return u.name;
}

function <user u> getAge() returns (int) {
    return u.age;
}



public type person record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    private:
    string ssn = "";
        int id = 0;
};


public function <person p> getName() returns (string) {
    return p.name;
}

function <person p> getAge() returns (int) {
    return p.age;
}

public function <person p> getSSN() returns (string) {
    return p.ssn;
}

public function <person p> setSSN(string ssn) {
    p.ssn = ssn;
}

public type employee record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    private:
        string ssn = "";
        int id = 0;
        int employeeId = 123456;
};

public function <employee e> getName() returns (string) {
    return e.name;
}

function <employee e> getAge() returns (int) {
    return e.age;
}

public function <employee e> getSSN() returns (string) {
    return e.ssn;
}

public function <employee e> setSSN(string ssn) {
    e.ssn = ssn;
}

public function <employee e> getEmployeeId() returns (int) {
    return e.employeeId;
}

