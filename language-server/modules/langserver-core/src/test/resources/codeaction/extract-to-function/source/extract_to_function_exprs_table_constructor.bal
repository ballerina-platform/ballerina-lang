type Employee record {
    readonly int id;
    string name;
    float salary;
};

type EmployeeTable table<Employee> key(id);

function testFunction() {
    EmployeeTable employeeTab = table [
        {id: 1, name: "John", salary: 300.50},
        {id: 2, name: "Bella", salary: 500.50}
    ];

    table<Employee> emplyeeTab2 = table key(id) [
        {name: "Cena", id: 3, salary: 0.0},
        {name: "Edward", id: 4, salary: 600}
    ];
}
