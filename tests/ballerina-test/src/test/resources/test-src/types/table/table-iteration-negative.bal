type Employee {
    int id;
    string name;
    float salary;
};

type EmployeeIncompatible {
    float id;
    string name;
    float salary;
};

type EmployeeSalary {
    int id;
    float salary;
};

type EmployeeSalaryIncompatible {
    float id;
    float salary;
};

function getSalaryInCompatibleInput(EmployeeIncompatible e) returns (EmployeeSalary) {
    EmployeeSalary s = {id: e.id, salary: e.salary};
    return s;
}

function getSalaryIncompatibleOutput(Employee e) returns (EmployeeSalaryIncompatible) {
    EmployeeSalaryIncompatible s = {id: e.id, salary: e.salary};
    return s;
}

function getSalaryIncompatibleInputOutput(EmployeeIncompatible e) returns (EmployeeSalaryIncompatible) {
    EmployeeSalaryIncompatible s = {id: e.id, salary: e.salary};
    return s;
}

function createTable() returns (table<Employee>) {
    table<Employee> dt = table{};

    Employee e1 = {id:1, name:"A", salary:100};
    Employee e2 = {id:2, name:"B", salary:200};
    Employee e3 = {id:3, name:"C", salary:300};

    _ = dt.add(e1);
    _ = dt.add(e2);
    _ = dt.add(e3);

    return dt;
}

function testInCompatibleInput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> t = dt.select(getSalaryInCompatibleInput);
    return t;
}

function testIncompatibleOutput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> t = dt.select(getSalaryIncompatibleOutput);
    return t;
}

function testIncompatibleInputOutput() returns (table) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> t = dt.select(getSalaryIncompatibleInputOutput);
    return t;
}
