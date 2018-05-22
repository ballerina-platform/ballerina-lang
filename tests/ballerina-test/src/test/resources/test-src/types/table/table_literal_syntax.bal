type Person {
    int id,
    int age,
    float salary,
    string name,
    boolean married,
};

table<Person> tGlobal;

function testTableDefaultValueForLocalVariable() returns (int) {
    table<Person> t1;
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    _ = t1.add(p1);
    int count = t1.count();
    return count;
}

function testTableDefaultValueForGlobalVariable() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    _ = tGlobal.add(p1);
    int count = tGlobal.count();
    return count;
}

function testTableAddOnUnconstrainedTable() returns (int) {
    table<Person> t1 = table {};
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    _ = t1.add(p1);
    int count = t1.count();
    return count;
}

function testTableAddOnConstrainedTable() returns (int) {
    table<Person> t1 = table {
        primaryKey: ["id", "salary"],
        index: ["id", "salary"]
    };

    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };
    _ = t1.add(p1);
    _ = t1.add(p2);
    int count = t1.count();
    return count;
}

function testValidTableVariable() returns (int) {
    table t1;
    table<Person> t2;
    return 0;
}

function testTableLiteralData() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "anne", married: true };
    Person p3 = { id: 3, age: 30, salary: 300.50, name: "peter", married: true };

    table<Person> t1 = table {
        primaryKey: ["id", "salary"],
        index: ["id", "salary"],
        data: [p1, p2, p3]
    };

    int count = t1.count();
    return count;
}

function testTableLiteralDataAndAdd() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "anne", married: true };
    Person p3 = { id: 3, age: 30, salary: 300.50, name: "peter", married: true };
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        primaryKey: ["id", "salary"],
        index: ["id", "salary"],
        data: [p1, p2, p3]
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testTableLiteralDataWithInit() returns (int) {
    table<Person> t1 = table {
        primaryKey: ["id", "salary"],
        index: ["id", "salary"],
        data: [1, 1]
    };

    int count = t1.count();
    return count;
}

function testTableAddOnConstrainedTableWithViolation() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        primaryKey: ["id"],
        index: ["id"],
        data: [p1, p2]
    };

    int count = t1.count();
    return count;
}

function testTableAddOnConstrainedTableWithViolation2() returns (string) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };
    Person p3 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        primaryKey: ["id"],
        index: ["id"],
        data: [p1, p2]
    };

    var ret = t1.add(p3);
    string s;
    match (ret) {
        error e => s = e.message;
        () => s = "nil";
    }
    return s;
}
