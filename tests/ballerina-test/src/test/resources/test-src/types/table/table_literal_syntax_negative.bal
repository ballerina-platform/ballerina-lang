type Person record {
    int id,
    int age,
    float salary,
    string name,
    boolean married,
};


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
