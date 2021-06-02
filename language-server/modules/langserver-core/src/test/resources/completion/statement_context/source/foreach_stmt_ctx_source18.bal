public function testFunction() {
    EmployeeTable employeeTab = table [
          {id: 1, name: "John", salary: 300.50},
          {id: 2, name: "Bella", salary: 500.50},
          {id: 3, name: "Peter", salary: 750.0}
    ];
    employeeTab.
}

type Employee record {
    readonly int id;
    string name;
    float salary;
};

type EmployeeTable table<Employee>;
