import ballerina/io;

// This type represents a person.
type Person record {
    int id,
    int age = -1,
    float salary,
    string name,
    boolean married,
};

// This type represents an order.
type Order record {
    int personId,
    int orderId,
    string items,
    float amount,
};

// This type represents the summed up order details.
type SummedOrder record {
    int personId,
    float amount,
};

// This type represents order details (this is derived by joining the person details
//and the order details).
type OrderDetails record {
    int orderId,
    string personName,
    string items,
    float amount,
};

// This type represents the Person public profile.
type PersonPublicProfile record {
    string knownName,
    int age = -1,
};

// This is the main function.
function main(string... args) {
    string queryStmt;

    // These are a few sample values that represent different `Person` records.
    Person p1 = { id: 1, age: 25, salary: 1000.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 26, salary: 1050.50, name: "kane", married: false };
    Person p3 = { id: 3, age: 27, salary: 1200.50, name: "jack", married: true };
    Person p4 = { id: 4, age: 28, salary: 1100.50, name: "alex", married: false };

    // This is the in-memory table that is constrained by the `Person` type.
    table<Person> personTable = table {
        { id, age, salary, name, married },
        [p1, p2, p3, p4]
    };

    // This prints the `Person` table content.
    printTable(queryStmt, "The personTable:  ", personTable);

    // These are a few sample values that represent orders made by the people who were
    // listed above as person records.
    Order o1 =
    { personId: 1, orderId: 1234, items: "pen, book, eraser", amount: 34.75 };
    Order o2 =
    { personId: 1, orderId: 2314, items: "dhal, rice, carrot", amount: 14.75 };
    Order o3 =
    { personId: 2, orderId: 5643, items: "Macbook Pro", amount: 2334.75 };
    Order o4 = { personId: 3, orderId: 8765, items: "Tshirt", amount: 20.75 };

    // This is the in-memory table that is constrained by the `Order` type.
    table<Order> orderTable = table {
        { personId, orderId, items, amount },
        [o1, o2, o3, o4]
    };

    // This prints the `Order` table content.
    printTable(queryStmt, "The orderTable: ", orderTable);

    // Querying for a table always returns a new in-memory table.

    //Queries all the records in a table and returns them as another in-memory table.
    table<Person> personTableCopy = from personTable select *;
    queryStmt = "\ntable<Person> personTableCopy = from personTable select *;";
    printTable(queryStmt,"personTableCopy: ", personTableCopy);

    //Queries all the records and returns them in the ascending order of the salary.
    table<Person> orderedPersonTable = from personTable select * order by salary;
    queryStmt = "\ntable<Person> orderedPersonTable = " +
            "from personTable select * order by salary;";
    printTable(queryStmt, "orderedPersonTable: ", orderedPersonTable);

    //Queries all the records in a table that match a specific filter criterion.
    table<Person> personTableCopyWithFilter =
                 from personTable where name == "jane" select *;
    queryStmt = "\ntable<Person> personTableCopyWithFilter = " +
            "from personTable where name == 'jane' select *;";
    printTable(queryStmt, "personTableCopyWithFilter: ", personTableCopyWithFilter);

    //Queries only few fields in a table and returns the results as a new in-memory
    //table constrained by a different type.
    table<PersonPublicProfile> childTable = from personTable
                  select name as knownName, age;
    queryStmt = "\ntable<PersonPublicProfile > childTable = " +
                    "from personTable select name as knownName, age;";
    printTable(queryStmt, "childTable: ", childTable);

    //This applies the `group by` clause to a table and returns a new table with the result.
    table<SummedOrder> summedOrderTable = from orderTable
                  select personId, sum(amount) group by personId;
    queryStmt = "\ntable<SummedOrder> summedOrderTable = " +
            "from orderTable select personId, sum(amount) group by personId;";
    printTable(queryStmt, "summedOrderTable: ", summedOrderTable);

    //Joins a table with another table and returns the selected fields in a table
    //constrained by a different type.
    table<OrderDetails> orderDetailsTable =
                    from personTable as tempPersonTable
                    join orderTable as tempOrderTable
                        on tempPersonTable.id == tempOrderTable.personId
                    select tempOrderTable.orderId as orderId,
                            tempPersonTable.name as personName,
                            tempOrderTable.items as items,
                            tempOrderTable.amount as amount;
    queryStmt = "\ntable<OrderDetails> orderDetailsTable = " +
            "from personTable as tempPersonTable
            join orderTable as tempOrderTable " +
                    "on tempPersonTable.id == tempOrderTable.personId
            select tempOrderTable.orderId as orderId, " +
                    "tempPersonTable.name as personName, " +
                    "tempOrderTable.items as items, " +
                    "tempOrderTable.amount as amount;";
    printTable(queryStmt, "orderDetailsTable: ", orderDetailsTable);

    //Joins a table with another table using the `where` clause and return the selected fields in a
    // table constrained by a different type.
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
            "from personTable where name != 'jane' as tempPersonTable
             join orderTable where personId != 3 as tempOrderTable " +
                    "on tempPersonTable.id == tempOrderTable.personId
             select tempOrderTable.orderId as orderId, " +
                    "tempPersonTable.name as personName," +
                    "tempOrderTable.items as items,
                    tempOrderTable.amount as amount;";
    printTable(queryStmt, "orderDetailsWithFilter: ", orderDetailsWithFilter);
}

function printTable(string stmt, string tableName, table returnedTable) {
    var retData = <json>returnedTable;
    io:println(stmt);
    io:print(tableName);
    match retData {
        json jsonRes => io:println(io:sprintf("%s", jsonRes));
        error e => io:println("Error in table to json conversion");
    }
}
