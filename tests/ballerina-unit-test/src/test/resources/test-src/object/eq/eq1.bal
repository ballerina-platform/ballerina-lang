
public type person object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getSSN() returns (string);

    public function setSSN(string s);
};


public function person.getName() returns (string) {
    return self.name;
}

public function person.getAge() returns (int) {
    return self.age;
}

public function person.getSSN() returns (string) {
    return self.ssn;
}

public function person.setSSN(string s) {
    self.ssn = s;
}

public type employee object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
    public int employeeId = 123456;

    public function __init (int age, string name) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getSSN() returns (string);

    public function setSSN(string s);

    public function getEmployeeId() returns (int);
};

public function employee.getName() returns (string) {
    return self.name;
}

public function employee.getAge() returns (int) {
    return self.age;
}

public function employee.getSSN() returns (string) {
    return self.ssn + ":employee";
}

public function employee.setSSN(string s) {
    self.ssn = s;
}

public function employee.getEmployeeId() returns (int) {
    return self.employeeId;
}

public type BarObj object {
    public int age = 0;
    public string name = "";

    public function getName() returns (string);

    function getAge() returns (int) {
        return self.age;
    }
};

public function BarObj.getName() returns (string) {
    return self.name;
}
