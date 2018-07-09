import ballerina/io;

type Employee record {
    int id,
    string name,
    float salary,
};

type EmployeeSalary record {
    int id,
    float salary,
};

function main(string... args) {
    // This creates an in-memory table constrained by the `Employee` type with id marked as the
    // primary key in column descriptor. Three data records are inserted to the table. Order of
    // the data values should match with the order of the column descriptor.
    table<Employee> tbEmployee = table {
        { primarykey id, name, salary },
        [ { 1, "Mary",  300.5 },
          { 2, "John",  200.5 },
          { 3, "Jim", 330.5 }
        ]
    };
    // This prints the table data.
    io:print("Table Information: ");
    io:println(tbEmployee);

    // Create Employee records.
    Employee e1 = { id: 1, name: "Jane", salary: 300.50 };
    Employee e2 = { id: 2, name: "Anne", salary: 100.50 };
    Employee e3 = { id: 3, name: "John", salary: 400.50 };
    Employee e4 = { id: 4, name: "Peter", salary: 150.0 };

    // This creates an in-memory table constrained by the `Employee` type with id as the primary key.
    // Two records are inserted to the table.
    table<Employee> tb = table {
        { primarykey id, name, salary },
        [e1, e2]
    };

    Employee[] employees = [e3, e4];
    // This adds the created records to the table.
    foreach (emp in employees) {
        var ret = tb.add(emp);
        match ret {
            () => io:println("Adding record to table successful");
            error e => io:println("Adding to table failed: " + e.message);
        }
    }

    // This prints the table data.
    io:print("Table Information: ");
    io:println(tb);

    // This accesses rows using the `foreach` loop.
    io:println("Using foreach: ");
    foreach x in tb {
        io:println("Name: " + x.name);
    }

    //This accesses rows using the `while` loop.
    io:println("Using while loop: ");
    while (tb.hasNext()) {
        var ret = <Employee>tb.getNext();
        match ret {
            Employee e => io:println("Name: " + e.name);
            error e => io:println("Error in get employee from table: "
                                  + e.message);
        }
    }

    // This derives the average salary using the iterable operations.
    float lowerAvgSal = tb.filter(isLowerSalary).map(getSalary).average();
    io:println("Average of Low salary: " + lowerAvgSal);

    //This selects a subset of columns from the table.
    table<EmployeeSalary> salaryTable = tb.select(getEmployeeSalary);

    //This fetches the table count using the count operation.
    int count = salaryTable.count();
    io:println("Selected row count: " + count);
    io:println(salaryTable);

    // This deletes the rows that match a given criteria.
    var ret = tb.remove(isLowerSalary);
    match ret {
        int rowCount => io:println("Deleted row count: " + rowCount);
        error e => io:println("Error in removing employees from table: "
                               + e.message);
    }
    io:print("After Delete: ");
    io:println(tb);

    // This converts the table to JSON format.
    var retValJson = <json>tb;
    match retValJson {
        json j => {
            io:print("JSON: ");
            io:println(j);
        }
        error e => io:println("Error in table to json conversion");
    }

    // This converts the table to XML format.
    var retValXml = <xml>tb;
    match retValXml {
        xml x => {
            io:print("XML: ");
            io:println(x);
        }
        error e => io:println("Error in table to xml conversion");
    }
}

function isLowerSalary(Employee p) returns boolean {
    return p.salary < 200;
}

function getSalary(Employee p) returns float {
    return p.salary;
}

function getEmployeeSalary(Employee e) returns (EmployeeSalary) {
    EmployeeSalary s = { id: e.id, salary: e.salary };
    return s;
}
