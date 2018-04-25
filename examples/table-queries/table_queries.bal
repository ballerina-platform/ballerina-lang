import ballerina/io;

// This type represents a person.
type Person {
    int id,
    int age = -1,
    float salary,
    string name,
    boolean married,
};

// This type represents an order.
type Order {
    int personId,
    int orderId,
    string items,
    float amount,
};

// This type represents the summed up order details.
type SummedOrder {
    int personId,
    float amount,
};

// This type represents order details (this is derived by joining the person details and the order details).
type OrderDetails {
    int orderId,
    string personName,
    string items,
    float amount,
};

// This type represents the Person publci profile.
type PersonPublicProfile {
    string knownName,
    int age = -1,
};

// main function
function main(string... args) {

    // The in-memory table which is constrained by the `Person` struct. 
    table<Person> personTable = table{};

    // The in-memory table which is constrained by the `Order` struct. 
    table<Order> orderTable = table{};

    // A few sample values which represent different persons. 
    Person p1 = {id: 1, age: 25, salary: 1000.50, name: "jane", married: true};
    Person p2 = {id: 2, age: 26, salary: 1050.50, name: "kane", married: false};
    Person p3 = {id: 3, age: 27, salary: 1200.50, name: "jack", married: true};
    Person p4 = {id: 4, age: 28, salary: 1100.50, name: "alex", married: false};
    Person[] personData = [p1, p2, p3, p4];

    // Insert the `Person` struct objects and populate the table.
    foreach (p in personData) {
        var ret = personTable.add(p);
        match ret {
            () => io:print("\nAdding to table successful");
            error err => io:println("Adding to table failed: " + err.message);
        }
    }

    // Print the Person table content.
    io:print("\nThe personTable: ");
    io:println(personTable);

    // A few sample values which represent orders made by the persons listed above. 
    Order o1 = {personId: 1, orderId: 1234, items: "pen, book, eraser", amount: 34.75};
    Order o2 = {personId: 1, orderId: 2314, items: "dhal, rice, carrot", amount: 14.75};
    Order o3 = {personId: 2, orderId: 5643, items: "Macbook Pro", amount: 2334.75};
    Order o4 = {personId: 3, orderId: 8765, items: "Tshirt", amount: 20.75};
    Order[] orderData = [o1, o2, o3, o4];

    // Insert the `Order` struct objects and populate the table.
    foreach (o in orderData) {
        var ret = orderTable.add(o);
        match ret {
            () => io:print("\nAdding to order successful");
            error err => io:println("Adding to order failed: " + err.message);
        }
    }

    // Print the Order table content.
    io:print("\nThe orderTable: ");
    io:println(orderTable);

    // Querying a table will always return a new in-memory table.

    // 1. Query all records from a table and return it as another in-memory table.
    table<Person> personTableCopy = from personTable select *;
    io:println("\ntable<Person> personTableCopy = from personTable select *;");
    io:print("personTableCopy: ");
    var ret = <json>personTableCopy;
    printTable(ret);

    // 2. Query all records in ascending order of salary.
    table<Person> orderedPersonTableCopy = from personTable select * order by salary;
    io:println("\ntable<Person> orderedPersonTableCopy = from personTable select * order by salary;");
    io:print("orderedPersonTableCopy: ");
    ret = <json>orderedPersonTableCopy;
    printTable(ret);

    // 3. Query all records from a table and return it as another in-memory table.
    table<Person> personTableCopyWithFilter = from personTable where name == "jane"
    select *;
    io:println("\ntable<Person> personTableCopyWithFilter = from personTable where name == 'jane' select *;");
    io:print("personTableCopyWithFilter: ");
    ret = <json>personTableCopyWithFilter;
    printTable(ret);

    // 4. Query only new fields from a table and return it as a new in-memory table constrained by a different struct.
    table<PersonPublicProfile> childTable = from personTable
    select name as knownName, age;
    io:println("\ntable<PersonPublicProfile > childTable = from personTable select name as knownName, age;");
    io:print("childTable: ");
    ret = <json>childTable;
    printTable(ret);

    // 5. Use the `group by` clause on a table and return a new table with the result.
    table<SummedOrder> summedOrderTable = from orderTable
    select personId, sum(amount) group by personId;
    io:println("\ntable<SummedOrder> summedOrderTable = from orderTable select personId, sum(amount) group by
    personId;");
    io:print("summedOrderTable: ");
    ret = <json>summedOrderTable;
    printTable(ret);

    // 6. Join a table with another table and return the selected fields in a table constrained by a different struct.
    table<OrderDetails> orderDetailsTable = from personTable as tempPersonTable
    join orderTable as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
    select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as
    items, tempOrderTable.amount as amount;
    io:println("\ntable<OrderDetails> orderDetailsTable = from personTable as tempPersonTable
            join orderTable as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
            select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as
            items, tempOrderTable.amount as amount;");
    io:print("orderDetailsTable: ");
    ret = <json>orderDetailsTable;
    printTable(ret);

    // 7. Join a table with another table using the `where` clause and return the selected fields in a 
    // table constrained by a different struct.
    table<OrderDetails> orderDetailsWithFilter = from personTable
    where name != "jane" as tempPersonTable join orderTable
    where personId != 3 as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
    select tempOrderTable.orderId as orderId, tempPersonTable.name as personName,
    tempOrderTable.items as items, tempOrderTable.amount as amount;
    io:println("\ntable<OrderDetails> orderDetailsWithFilter = from personTable where name != 'jane' as tempPersonTable
        join orderTable where personId != 3 as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
        select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as items,
        tempOrderTable.amount as amount;");
    io:print("orderDetailsWithFilter: ");
    ret = <json>orderDetailsWithFilter;
    printTable(ret);
}

function printTable(json|error retData) {
    match retData {
        json jsonRes => io:println(io:sprintf("%s", jsonRes));
        error e => io:println("Error in table to json conversion");
    }
}
