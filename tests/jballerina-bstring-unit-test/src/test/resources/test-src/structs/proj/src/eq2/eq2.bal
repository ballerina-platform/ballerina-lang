
public type employee record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "95134";
    string ssn = "";
    int id = 0;
    int employeeId = 123456;
};

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