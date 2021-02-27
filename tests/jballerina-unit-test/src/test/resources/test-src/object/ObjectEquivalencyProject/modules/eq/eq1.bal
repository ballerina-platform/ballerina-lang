
public class person {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;

    public function getName() returns (string) {
        return self.name;
    }

    public function getAge() returns (int) {
        return self.age;
    }

    public function getSSN() returns (string) {
        return self.ssn;
    }

    public function setSSN(string s) {
        self.ssn = s;
    }
}

public class employee {
    public int age = 0;
    public string name = "";
    public string address = "";
    public string zipcode = "95134";
    public string ssn = "";
    public int id = 0;
    public int employeeId = 123456;

    public function init (int age, string name) {
        self.age = age;
        self.name = name;
    }

    public function getName() returns (string) {
        return self.name;
    }

    public function getAge() returns (int) {
        return self.age;
    }

    public function getSSN() returns (string) {
        return self.ssn + ":employee";
    }

    public function setSSN(string s) {
        self.ssn = s;
    }

    public function getEmployeeId() returns (int) {
        return self.employeeId;
    }
}

public class BarObj {
    public int age = 0;
    public string name = "";

    public function getName() returns (string) {
        return self.name;
    }

    function getAge() returns (int) {
        return self.age;
    }
}
