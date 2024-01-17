function testFunction() returns Employee {
    Employee[] emArray = [{
        name: {},
        salary: 150000
    }];
}

type Employee record {
    readonly record {
        string first;
        string last;
    } name;
    int salary;
};
