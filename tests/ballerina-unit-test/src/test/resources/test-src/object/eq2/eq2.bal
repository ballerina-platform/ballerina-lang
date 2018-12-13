
public type employee object {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
    public int employeeId = 123456;

    public function getName() returns (string);

    public function getAge() returns (int);

    public function getSSN() returns (string);

    public function setSSN(string s);

    public function getEmployeeId() returns (int);
};

function employee.getName() returns (string) {
    return self.name;
}

function employee.getAge() returns (int) {
    return self.age;
}

function employee.getSSN() returns (string) {
    return self.ssn + ":employee";
}

function employee.setSSN(string s) {
    self.ssn = s;
}

function employee.getEmployeeId() returns (int) {
    return self.employeeId;
}

public type FooObj object {
    public int age = 0;
    public string name = "";

    public function getName() returns (string);
};

function FooObj.getName() returns (string) {
    return self.name;
}
