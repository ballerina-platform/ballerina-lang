type Employee record {
    int id;
    string name;
    float salary;
};

type EmployeeSalary record {
    int id;
    float salary;
};

public function main(string... args) {
    table<Employee> tbEmployee = table {
        { key id, name, salary },
        [ { 1, "Mary",  300.5 },
          { 2, "John",  200.5 },
          { 3, "Jim", 330.5 }
        ]
    };

    tbEmployee.
}
