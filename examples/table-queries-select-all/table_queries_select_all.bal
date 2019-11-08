import ballerina/io;

// This `record` type represents a person.
type Person record {
    int id;
    int age = -1;
    float salary;
    string name;
    boolean married;
};

public function main() {
    string queryStmt = "";

    // These are a few sample values that represent different `Person` records.
    Person p1 =
    {id: 1, age: 25, salary: 1000.50, name: "jane", married: true};
    Person p2 =
    {id: 2, age: 26, salary: 1050.50, name: "kane", married: false};
    Person p3 =
    {id: 3, age: 27, salary: 1200.50, name: "jack", married: true};
    Person p4 =
    {id: 4, age: 28, salary: 1100.50, name: "alex", married: false};

    // This is the in-memory `table` that is constrained by the `Person` type.
    table<Person> personTable = table {
        {id, age, salary, name, married},
        [p1, p2, p3, p4]
    };

    // This prints the content of the `Person` table.
    printTable(queryStmt, "The personTable:  ", personTable);

    // Querying a `table` always returns a new in-memory `table`.
    // Queries all the records in a `table` and returns them as another in-memory `table`.
    table<Person> personTableCopy = from personTable select *;
    queryStmt = "\ntable<Person> personTableCopy = from personTable select *;";
    printTable(queryStmt,"personTableCopy: ", personTableCopy);
}

function printTable(string stmt, string tableName, table<anydata> returnedTable) {
    io:println(stmt);
    io:println(tableName);
    foreach var row in returnedTable {
       io:println(row);
    }
}
