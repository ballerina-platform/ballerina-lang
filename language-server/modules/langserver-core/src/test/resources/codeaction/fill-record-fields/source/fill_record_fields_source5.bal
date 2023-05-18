function testFunction() returns Employee {
    table<Employee> key() st = table [
        {
            name: {},
            salary: 0
        }
    ];
}

type Employee record {
    readonly record {
        string first;
        string last;
    } name;
    int salary;
};
