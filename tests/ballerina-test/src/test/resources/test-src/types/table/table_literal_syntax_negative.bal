type Person record {
    int id,
    int age,
    float salary,
    string name,
    boolean married,
    !...
};

type Employee object {

    public int id = 1;
    public int age = 10,
    public string name = "sample name";


    new(id, age, name) {
    }
};

type test table<Employee>;

function testTableLiteralDataAndAdd2() returns (int) {
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        { primarykey id, primarykey salary, name, age, married2 },
        [{ 1, 300.5, "jane",  30, true },
        { 2, 302.5, "anne",  23, false },
        { 3, 320.5, "john",  33, true }
        ]
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testTableLiteralDataWithInit() returns (int) {
    table<Person> t1 = table {
        { primarykey id, primarykey salary, name, age, married },
        [1, 1]
    };

    int count = t1.count();
    return count;
}

function testTableLiteralDataAndAddWithObject() returns (int) {
    Employee p4 = new Employee(4, 24, "Paul");
    Employee p5 = new Employee(5, 30, "mary");

    //Object types cannot be included in the literal
    table<Employee> t1 = table {
        { primarykey id, name, age }
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testEmptyTableCreateInvalid() {
    table t1 = table{};
}
