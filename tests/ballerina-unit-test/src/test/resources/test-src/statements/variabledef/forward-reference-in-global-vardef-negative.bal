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

(Employee, Person) pp = (employee, person);

Employee employee = {
    name: person.name,
    age: person.age,
    empNo: 100
};

Person person = {
    name: "Sumedha",
    age: 30
};




public function getEmployee() returns Employee {
    //Employee employee = {
    //    name: person.name,
    //    age: person.age,
    //    empNo: 100
    //};
    //
    //Person person = {
    //    name: "Sumedha",
    //    age: 30
    //};
    return employee;
}

public function main() {
    var e = getEmployee();
}
