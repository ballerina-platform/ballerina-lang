
public type employee object {
    public {
        int age;
        string name;
        string address;
        string zipcode = "95134";
        string ssn;
        int id;
        int employeeId = 123456;
    }

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getSSN() returns (string);

    public function setSSN(string ssn);

    public function getEmployeeId() returns (int);
};

public function employee::getName() returns (string) {
    return self.name;
}

public function employee::getAge() returns (int) {
    return self.age;
}

public function employee::getSSN() returns (string) {
    return self.ssn + ":employee";
}

public function employee::setSSN(string ssn) {
    self.ssn = ssn;
}

public function employee::getEmployeeId() returns (int) {
    return self.employeeId;
}

public type FooObj object {
    public {
        int age;
        string name;
    }

    public function getName() returns (string);
};

public function FooObj::getName() returns (string) {
    return self.name;
}
