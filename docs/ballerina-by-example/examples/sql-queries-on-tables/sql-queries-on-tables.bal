import ballerina/io;

// struct to represent a person in the example.
type Person {
    int id;
    int age = -1;
    float salary;
    string name;
    boolean married;
};

// struct to represent an order in this example.
type Order {
    int personId;
    int orderId;
    string items;
    float amount;
};

// struct to represent summed order details.
type SummedOrder {
    int personId;
    float amount;
};

// struct to represent order details (this is derived by joining person details and order details
type OrderDetails {
    int orderId;
    string personName;
    string items;
    float amount;
};


type PersonPublicProfile {
    string knownName;
    int age = -1;
};

// main function
public function main (string[] args) {

    // The in memory table which is constrained by struct "Person"
    table<Person> personTable;
    // The in memory table which is constrained by struct "Order"
    table<Order> orderTable;

    // few sample values which represents different persons
    Person p1 = {id:1, age:25, salary:1000.50, name:"jane", married:true};
    Person p2 = {id:2, age:26, salary:1050.50, name:"kane", married:false};
    Person p3 = {id:3, age:27, salary:1200.50, name:"jack", married:true};
    Person p4 = {id:4, age:28, salary:1100.50, name:"alex", married:false};

    // few sample values which represents orders made by above persons
    Order o1 = {personId:1, orderId:1234, items:"pen, book, eraser", amount:34.75};
    Order o2 = {personId:1, orderId:2314, items:"dhal, rice, carrot", amount:14.75};
    Order o3 = {personId:2, orderId:5643, items:"Macbook Pro", amount:2334.75};
    Order o4 = {personId:3, orderId:8765, items:"Tshirt", amount:20.75};

    // Insert the person struct objects and populate the table.
    personTable.add(p1);
    personTable.add(p2);
    personTable.add(p3);
    personTable.add(p4);

    io:print("The personTable: ");
    io:println(personTable);

    // Insert the order struct objects and populate the table
    orderTable.add(o1);
    orderTable.add(o2);
    orderTable.add(o3);
    orderTable.add(o4);

    io:print("The orderTable: ");
    io:println(orderTable);

    // Querying a table will always return a new in memory table.

    // 1. Query all records from a table and return it as another in memory table.
    table<Person> personTableCopy = from personTable
                                         select *;
    io:println("table<Person> personTableCopy = from personTable select *;");
    io:print("personTableCopy: ");
    io:println(personTableCopy);

    // 2. Query all records in ascending order of salary.
    table<Person> orderedPersonTableCopy = from personTable
                                         select * order by salary;
    io:println("table<Person> orderedPersonTableCopy = from personTable select * order by salary;");
    io:print("orderedPersonTableCopy: ");
    io:println(orderedPersonTableCopy);

    // 3. Query all records from a table and return it as another in memory table.
    table<Person> personTableCopyWithFilter = from personTable where name == "jane"
                                         select *;
    io:println("table<Person> personTableCopyWithFilter = from personTable where name == 'jane' select *;");
    io:print("personTableCopyWithFilter: ");
    io:println(personTableCopyWithFilter);

    // 4. Query only a new fields from a table and return it as a new in memory table constrained by a different struct.
    table<PersonPublicProfile > childTable = from personTable
                                                  select name as knownName, age;
    io:println("table<PersonPublicProfile > childTable = from personTable select name as knownName, age;");
    io:print("childTable: ");
    io:println(childTable);

    // 5. Perform group by on a table and return a neww table with the result.
    table<SummedOrder> summedOrderTable = from orderTable
                                               select personId, sum(amount) group by personId;
    io:println("table<SummedOrder> summedOrderTable = from orderTable select personId, sum(amount) group by personId;");
    io:print("summedOrderTable: ");
    io:println(summedOrderTable);

    // 6. Join a table with another table and return the selected fields in a table constrained by a different struct.
    table<OrderDetails> orderDetailsTable = from personTable as tempPersonTable
            join orderTable as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
            select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as
            items, tempOrderTable.amount as amount;
    io:println("table<OrderDetails> orderDetailsTable = from personTable as tempPersonTable
            join orderTable as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
            select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as
            items, tempOrderTable.amount as amount;");
    io:print("orderDetailsTable: ");
    io:println(orderDetailsTable);

    // 7. Join a table with another table and return the selected fields in a table constrained by a different struct
    //    with where clauses
    table<OrderDetails> orderDetailsWithFilter = from personTable where name != "jane" as tempPersonTable
        join orderTable where personId != 3 as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
        select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as items,
        tempOrderTable.amount as amount;
    io:println("table<OrderDetails> orderDetailsWithFilter = from personTable where name != 'jane' as tempPersonTable
        join orderTable where personId != 3 as tempOrderTable on tempPersonTable.id == tempOrderTable.personId
        select tempOrderTable.orderId as orderId, tempPersonTable.name as personName, tempOrderTable.items as items,
        tempOrderTable.amount as amount;");
    io:print("orderDetailsWithFilter: ");
    io:println(orderDetailsWithFilter);
}
