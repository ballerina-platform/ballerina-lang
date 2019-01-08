import ballerina/log;
import ballerina/io;

public type Person record {
    string name = "";
    int age = 0;
};

public type Employee record {
    string name = "";
    int age = 0;
    int empNo = 0;
};

public function getEmployee2() returns Employee {
    return employee;
}

public Employee employee = {
    name: person.name,
    age: person.age,
    empNo: 100
};

(Employee, Person) pp = (employee, person);

public function getEmployee() returns Employee {
    return employee;
}

public function main1() {
    var e = getEmployee();
    var e2 = getEmployee2();
    log:printInfo("end");
    io:println("end");
}

int dep1 = dep2;
