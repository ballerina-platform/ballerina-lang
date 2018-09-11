type Person record {
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
        { primarykey id, primarykey salary, name, age, married }
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
        { primarykey id, primarykey salary, name, age, married },
        [p1, p2, p3]
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
        { primarykey id, primarykey salary, name, age, married },
        [p1, p2, p3]
    };

    _ = t1.add(p4);
    _ = t1.add(p5);

    int count = t1.count();
    return count;
}

function testTableLiteralDataAndAdd2() returns (int) {
    Person p4 = { id: 4, age: 30, salary: 300.50, name: "john", married: true };
    Person p5 = { id: 5, age: 30, salary: 300.50, name: "mary", married: true };

    table<Person> t1 = table {
        { primarykey id, primarykey salary, name, age, married },
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

function testTableAddOnConstrainedTableWithViolation() returns (int) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        { primarykey id, salary, name, age, married },
        [p1, p2]
    };

    int count = t1.count();
    return count;
}

function testTableAddOnConstrainedTableWithViolation2() returns (string) {
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };
    Person p3 = { id: 2, age: 30, salary: 300.50, name: "jane", married: true };

    table<Person> t1 = table {
        { primarykey id, salary, name, age, married },
        [p1, p2]
    };

    var ret = t1.add(p3);
    string s;
    match (ret) {
        error e => s = e.message;
        () => s = "nil";
    }
    return s;
}

function testTableAddWhileIterating() returns (int, int) {
    table<Person> t1 = table {
        { primarykey id, primarykey salary, name, age, married }
    };

    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 350.50, name: "jane", married: true };
    _ = t1.add(p1);

    int loopVariable = 0;

    while(t1.hasNext()) {
        loopVariable = loopVariable + 1;
        _ = t1.add(p2);
        any data = t1.getNext();
    }
    int count = t1.count();
    return (loopVariable, count);
}

function testUnconstraintTable() returns (int, json, xml, int, int, any, error?, int|error) {
    table t1;
    //iterable operation
    int count1 = t1.count();
    //json conversion
    json j = check <json>t1;
    //xml conversion
    xml x = check <xml>t1;
    //Iterate with while loop
    int iter1 = 0;
    while (t1.hasNext()) {
        var data = t1.getNext();
        iter1 = iter1 + 1;
    }
    //Iterate with foreach
    int iter2 = 0;
    foreach datarow in t1 {
        iter2 = iter2 + 1;
    }
    //Get next row
    any row = t1.getNext();
    //Add data
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    error? e1 = t1.add(p1);
    //Remove data
    int|error e2 = t1.remove(isBellow35);

    return (count1, j, x, iter1, iter2, row, e1, e2);
}

function isBellow35(Person p) returns (boolean) {
    return p.age < 35;
}
