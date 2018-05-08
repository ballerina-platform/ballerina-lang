
public type person {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
};


public function <person p> getName() returns (string) {
    return p.name;
}

public function <person p> getAge() returns (int) {
    return p.age;
}

public function <person p> getSSN() returns (string) {
    return p.ssn;
}

public function <person p> setSSN(string ssn) {
    p.ssn = ssn;
}

public type employee {
    int age;
    string name;
    string address;
    string zipcode = "95134";
    string ssn;
    int id;
    int employeeId = 123456;
};

public function <employee e> employee() {
}

public function <employee e> getName() returns (string) {
    return e.name;
}

public function <employee e> getAge() returns (int) {
    return e.age;
}

public function <employee e> getSSN() returns (string) {
    return e.ssn + ":employee";
}

public function <employee e> setSSN(string ssn) {
    e.ssn = ssn;
}

public function <employee e> getEmployeeId() returns (int) {
    return e.employeeId;
}