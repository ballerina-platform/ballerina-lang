import ballerina/io;

// This `record` type represents a person.
type Person record {
    int id;
    int age = -1;
    float salary;
    string name;
    boolean married;
};

// This `record` type represents an order.
type Order record {
    int personId;
    int orderId;
    string items;
    float amount;
};

// This `record` type represents the order details (derived by joining the person details
//and the order details).
type OrderDetails record {
    int orderId;
    string personName;
    string items;
    float amount;
};

public function main() {
    string queryStmt = "";

    // These are a few sample values, which represent different `Person` records.
    Person p1 =
    {id: 1, age: 25, salary: 1000.50, name: "jane", married: true};
    Person p2 =
    {id: 2, age: 26, salary: 1050.50, name: "kane", married: false};
    Person p3 =
    {id: 3, age: 27, salary: 1200.50, name: "jack", married: true};
    Person p4 =
    {id: 4, age: 28, salary: 1100.50, name: "alex", married: false};

    // This is the in-memory `table`, which is constrained by the `Person` type.
    table<Person> personTable = table {
        {id, age, salary, name, married},
        [p1, p2, p3, p4]
    };

    // This prints the content of the `Person` table.
    printTable(queryStmt, "The personTable:  ", personTable);

    // These are a few sample values, which represent orders made by the 
    //  person records listed above.
    Order o1 =
    {personId: 1, orderId: 1234, items: "pen, book, eraser", amount: 34.75};
    Order o2 =
    {personId: 1, orderId: 2314, items: "dhal, rice, carrot", amount: 14.75};
    Order o3 =
    {personId: 2, orderId: 5643, items: "Macbook Pro", amount: 2334.75};
    Order o4 =
    {personId: 3, orderId: 8765, items: "Tshirt", amount: 20.75};

    // This is the in-memory `table`, which is constrained by the `Order` type.
    table<Order> orderTable = table {
        {personId, orderId, items, amount},
        [o1, o2, o3, o4]
    };

    // This prints the content of the `Order` table.
    printTable(queryStmt, "The orderTable: ", orderTable);


    // Querying a `table` always returns a new in-memory `table`.
    // Joins a `table` with another `table` using the `where` clause and returns the selected fields in a
    // `table` constrained by a different type.
    table<OrderDetails> orderDetailsWithFilter =
                    from personTable
                    where name != "jane" as tempPersonTable
                    join orderTable where personId != 3 as tempOrderTable
                            on tempPersonTable.id == tempOrderTable.personId
                    select tempOrderTable.orderId as orderId,
                            tempPersonTable.name as personName,
                            tempOrderTable.items as items,
                            tempOrderTable.amount as amount;
    queryStmt = "\ntable<OrderDetails> orderDetailsWithFilter = " +
            "from personTable where name != 'jane' as tempPersonTable " +
             "join orderTable where personId != 3 as tempOrderTable " +
                    "on tempPersonTable.id == tempOrderTable.personId " +
             "select tempOrderTable.orderId as orderId, " +
                    "tempPersonTable.name as personName, " +
                    "tempOrderTable.items as items, " +
                    "tempOrderTable.amount as amount;";
    printTable(queryStmt, "orderDetailsWithFilter: ", orderDetailsWithFilter);
}

function printTable(string stmt, string tableName, table<anydata> returnedTable) {
    io:println(stmt);
    io:println(tableName);
    foreach var row in returnedTable {
       io:println(row);
    }
}
