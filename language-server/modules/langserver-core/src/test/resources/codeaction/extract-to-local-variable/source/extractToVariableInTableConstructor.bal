type Employee record {
    readonly string name;
    int salary;
};

table<Employee> key(name) t = table [
    { name: "John", salary: 100 },
    { name: "Jane", salary: 200 }
];
