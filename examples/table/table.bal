import ballerina/io;

type Employee record {
    int id;
    string name;
    float salary;
};

type EmployeeSalary record {
    int id;
    float salary;
};

public function main() {
    // This creates an in-memory `table` constrained by the `Employee` type with the `id` marked as the
    // primary key in the column descriptor. Three data records are inserted into the `table`. The order of
    // the data values should match the order of the column descriptor.
    table<Employee> tbEmployee = table {
        { key id, name, salary },
        [ { 1, "Mary",  300.5 },
          { 2, "John",  200.5 },
          { 3, "Jim", 330.5 }
        ]
    };
    // This prints the `table` data.
    io:print("Table Information: ");
    io:println(tbEmployee);

    // Creates `Employee` records.
    Employee e1 = { id: 1, name: "Jane", salary: 300.50 };
    Employee e2 = { id: 2, name: "Anne", salary: 100.50 };
    Employee e3 = { id: 3, name: "John", salary: 400.50 };
    Employee e4 = { id: 4, name: "Peter", salary: 150.0 };

    // This creates an in-memory `table` constrained by the `Employee` type with the `id` as the primary key.
    // Two records are inserted into the `table`.
    table<Employee> tb = table {
        { key id, name, salary },
        [e1, e2]
    };

    Employee[] employees = [e3, e4];
    // This adds the created records to the `table`.
    foreach var emp in employees {
        var ret = tb.add(emp);
        if (ret is ()) {
            io:println("Adding record to table successful");
        } else {
            io:println("Adding to table failed: " + ret.reason());
        }
    }

    // This prints the `table` data.
    io:println("Table Information: ", tb);

    // This accesses rows using the `foreach` loop.
    io:println("Using foreach: ");
    foreach var x in tb {
        io:println("Name: " + x.name);
    }

    // This accesses rows using the `while` loop.
    io:println("Using while loop: ");
    while (tb.hasNext()) {
        var ret = Employee.convert(tb.getNext());
        if (ret is Employee) {
            io:println("Name: " + ret.name);
        } else {
            io:println("Error in get employee from table: "
                                  + ret.reason());
        }
    }

    // This derives the average salary using iterable operations.
    float lowerAvgSal = tb.filter(isLowerSalary).map(getSalary).average();
    io:println("Average of Low salary: " + lowerAvgSal);

    // This selects a subset of columns from the `table`.
    table<EmployeeSalary> salaryTable = tb.select(getEmployeeSalary);

    // This fetches the `table` row count using the `count` operation.
    int count = salaryTable.count();
    io:println("Selected row count: " + count);
    io:println(salaryTable);

    // This deletes the rows that match a given criteria.
    var ret = tb.remove(isLowerSalary);
    if (ret is int) {
        io:println("Deleted row count: " + ret);
    } else {
        io:println("Error in removing employees from table: "
                               + ret.reason());
    }
    io:println("After Delete: ", tb);

    // This converts the `table` to JSON format.
    var retValJson = json.convert(tb);
    if (retValJson is json) {
        io:println("JSON: ", retValJson);
    } else {
        io:println("Error in table to json conversion");
    }

    // This converts the `table` to XML format.
    var retValXml = xml.convert(tb);
    if (retValXml is xml) {
        io:println("XML: ", retValXml);
    } else {
        io:println("Error in table to xml conversion");
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
