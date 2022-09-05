type Employee record {
    readonly string name;
    int salary;
};

table<Employee> key(name) t = table [
    { name: "John", salary: 100 },
    { name: "Jane", salary: 200 }
];

type EmployeeTable table<Employee> key(name);

table<Employee> t2 = table key(name) [
        {name: "Cena", salary: 0},
        {name: "Edward", salary: 600}
    ];
}