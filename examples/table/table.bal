import ballerina/io;

type Employee {
    int id,
    string name,
    float salary,
};

function main(string... args) {
    // Create an in-memory table constrained by the Employee type.
    table<Employee> tb = table {};

    // Add some data rows to the table.
    Employee e1 = {id: 1, name: "Jane", salary: 300.50};
    Employee e2 = {id: 2, name: "Anne", salary: 100.50};
    Employee e3 = {id: 3, name: "John", salary: 400.50};
    Employee e4 = {id: 4, name: "Peter", salary: 150.0};

    _ = tb.add(e1);
    _ = tb.add(e2);
    _ = tb.add(e3);
    _ = tb.add(e4);

    // Print the table data.
    io:print("Table Information: ");
    io:println(tb);

    // Access rows using the 'foreach' loop.
    foreach x in tb {
        io:println("Name: " + x.name);
    }

    // Find the average salary using the iterable operations.
    float lowerAvgSal = tb.filter(isLowerSalary).map(getSalary).average();
    io:println("Average of Low salary: " + lowerAvgSal);

    // Delete the rows that match a given criteria.
    int count = check tb.remove(isLowerSalary);
    io:println("Deleted row count: " + count);
    io:print("After Delete: ");
    io:println(tb);

    // Convert to JSON.
    json j = check <json>tb;

    io:print("JSON: ");
    io:println(j);

    // Convert to XML.
    xml x = check <xml>tb;

    io:print("XML: ");
    io:println(x);
}

function isLowerSalary(Employee p) returns boolean {
    return p.salary < 200;
}

function getSalary(Employee p) returns float {
    return p.salary;
}
