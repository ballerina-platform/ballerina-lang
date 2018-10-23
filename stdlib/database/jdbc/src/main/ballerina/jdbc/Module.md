## Module overview

This module provides the functionality required to access and manipulate data stored in any type of relational database that is accessible via Java Database Connectivity (JDBC). 

### Endpoint 

To access a database, you must first create an `endpoint`, which is a virtual representation of the physical endpoint that you are trying to connect to. Create an endpoint of the JDBC client type (i.e., `jdbc:Client`) and provide the necessary connection parameters. This will create a pool of connections to the specified database. A sample for creating an endpoint with a JDBC client can be found below. 

**NOTE**: Although the JDBC client type supports connecting to any type of relational database that is accessible via JDBC, if you are using a MySQL or H2 database, it is recommended to use endpoints that are created using the client types specific to them via the relevant Ballerina modules.

### Database operations

Once the endpoint is created, database operations can be executed through that endpoint. This module provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. Samples for these operations can be found below. Details of the SQL data types and query parameters relevant for these database operations can be found in the documentation for the SQL module. 


## Samples

### Creating an endpoint
```ballerina
endpoint jdbc:Client testDB {
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
};
```
The full list of endpoint properties can be found listed under the `sql:PoolOptions` type, which is located in the `types.bal` file of the SQL module directory.

### Creating tables

This sample creates a table with two columns. One column is of type `int`, and the other is of type `varchar`. The CREATE statement is executed via the `update` operation of the endpoint.

```ballerina
// Create the ‘Students’ table with fields ‘id’, 'name' and ‘age’.
var ret = testDB->update("CREATE TABLE IF NOT EXISTS Students(id int AUTO_INCREMENT, name varchar(255), age int, PRIMARY KEY(id))");
match ret {
    int retInt => io:println("Students table create status in DB: " + retInt);
    error err => io:println("Students table create failed: " + err.message);
}
```

### Inserting data

This sample shows three examples of data insertion by executing an INSERT statement using the `update` operation of the endpoint. 

In the first example, query parameter values are passed directly into the query statement of the `update` operation:

```ballerina
var ret1 = testDB->update("INSERT INTO Students(name, age) values ('john', 7)");
match ret1 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

In the second example, the parameter values, which are in local variables, are passed directly as parameters to the `update` operation. This direct parameter passing can be done for any primitive Ballerina type like string, int, float, or boolean. The sql type of the parameter is derived from the type of the Ballerina variable that is passed in. 

```ballerina
string name = "Anne";
int age = 8;
var ret2 = testDB->update("INSERT INTO Students(name, age) values (?, ?)", name, age);
match ret2 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

In the third example, parameter values are passed as an `sql:Parameter` to the `update` operation. Use `sql:Parameter` when you need to provide more details such as the exact SQL type of the parameter, or the parameter direction. The default parameter direction is "IN". For more details on parameters, see the `sql` module.

```ballerina
sql:Parameter p1 = { sqlType: sql:TYPE_VARCHAR, value: "James" };
sql:Parameter p2 = { sqlType: sql:TYPE_INTEGER, value: 10 };
var ret3 = testDB->update("INSERT INTO Students(name, age) values (?, ?)", p1, p2);
match ret3 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

### Inserting data with auto-generated keys

This example demonstrates inserting data while returning the auto-generated keys. It achieves this by using the `updateWithGeneratedKeys` operation to execute the INSERT statement.

```ballerina
int age = 31;
string name = "Kate";
var ret0 = testDB->updateWithGeneratedKeys("INSERT INTO Students(name, age) values (?, ?)", (), name, age);
match ret0 {
    (int, string[]) y => {
        var (count, ids) = y;
        io:println("Inserted row count: " + count);
        io:println("Generated key: " + ids[0]);
    }
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

### Selecting data

This example demonstrates selecting data. First, a type is created to represent the returned result set. Next, the SELECT query is executed via the `select` operation of the endpoint by passing that result set type. Once the query is executed, each data record can be retrieved by looping the result set. The table returned by the select operation holds a pointer to the actual data in the database and it loads data from the table only when it is accessed. This table can be iterated only once. 

```ballerina
// Define a type to represent the results set.
type Student record {
    int id,
    string name,
    int age,
};

// Select the data from the table.
var selectRet = testDB->select("SELECT * FROM Students WHERE id = 1", Student);
table<Student> dt;
match selectRet {
    table tableReturned => dt = tableReturned;
    error err => io:println("Select data from Students table failed: " + err.message);
}

// Access the returned table.
foreach entry in dt {
    io:println("Student:Name:" + entry.name);
}
```

To re-iterate the same table multiple times, set the `loadToMemory` argument to true within the `select` action.

```ballerina
var selectRet = testDB->select("SELECT * FROM Students", Student, loadToMemory = true);
table<Student> dt;
match selectRet {
    table tableReturned => dt = tableReturned;
    error err => io:println("Select data from Students table failed: " + err.message);
}

foreach entry in dt {
    io:println("Student:" + entry.id + "|" + entry.name + "|" + entry.age);
}
foreach entry in dt {
    io:println("Student:" + entry.id + "|" + entry.name + "|" + entry.age);
}
````

### Updating data

This example demonstrates modifying data by executing an UPDATE statement via the `update` operation of the endpoint.

```ballerina
var ret4 = testDB->update("Update Students set name = 'Johnes' where id = ?", 1);
match ret4 {
    int retInt => io:println("Updated row count in Students table: " + retInt);
    error err => io:println("Update in Students table failed: " + err.message);
}
```

### Batch updating data

This example demonstrates how to insert multiple records with a single INSERT statement that is executed via the `batchUpdate` operation of the endpoint. This is done by first creating multiple parameter arrays, each representing a single record, and then passing those arrays to the `batchUpdate` operation. Similarly, multiple UPDATE statements can also be executed via `batchUpdate`.

```ballerina
// Create the first batch of parameters.
sql:Parameter para1 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
sql:Parameter para2 = { sqlType: sql:TYPE_INTEGER, value: 12 };
sql:Parameter[] parameters1 = [para1, para2];

// Create the second batch of parameters.
sql:Parameter para3 = { sqlType: sql:TYPE_VARCHAR, value: "Peter" };
sql:Parameter para4 = { sqlType: sql:TYPE_INTEGER, value: 6 };
sql:Parameter[] parameters2 = [para3, para4];

// Do the batch update by passing the batches.
var ret5 = testDB->batchUpdate("INSERT INTO Students(name, age) values (?, ?)", parameters1, parameters2);
match ret5 {
    int[] counts => {
        io:println("Batch item 1 update counts: " + counts[0]);
        io:println("Batch item 2 update counts: " + counts[1]);
    }
    error err => io:println("Batch update action failed: " + err.message);
}
```

### Calling stored procedures

The following examples demonstrate executing stored procedures via the `call` operation of the endpoint. 

The first example shows how to create and call a simple stored procedure that inserts data.
```ballerina
// Create the stored procedure.
var ret6 = testDB->update("CREATE PROCEDURE INSERTDATA (IN pName VARCHAR(255), IN pAge INT)
                           BEGIN
                              INSERT INTO Students(name, age) values (pName, pAge);
                           END");
match ret6 {
    int status => io:println("Stored proc creation status: " + status);
    error err => io:println("Stored procedure creation failed: " + err.message);
}

// Call the stored procedure.
var ret7 = testDB->call("{CALL INSERTDATA(?,?)}", (), "George", 15);
match ret7 {
    ()|table[] => io:println("Call action successful");
    error err => io:println("Stored procedure call failed: " + err.message);
}
```
This next example shows how to create and call a stored procedure that accepts `INOUT` and `OUT` parameters. 

```ballerina
// Create the stored procedure.
var ret8 = testDB->update("CREATE PROCEDURE GETCOUNT (INOUT pID INT, OUT pCount INT)
                           BEGIN
                                SELECT COUNT(*) INTO pID FROM Students WHERE id = pID;
                                SELECT COUNT(*) INTO pCount FROM Students WHERE id = 2;
                           END");
match ret8 {
    int status => io:println("Stored proc creation status: " + status);
    error err => io:println("Stored procedure creation failed: " + err.message);
}
//
// Call the stored procedure.
sql:Parameter param1 = { sqlType: sql:TYPE_INTEGER, value: 3, direction: sql:DIRECTION_INOUT };
sql:Parameter param2 = { sqlType: sql:TYPE_INTEGER, direction: sql:DIRECTION_OUT };
var ret9 = testDB->call("{CALL GETCOUNT(?,?)}", (), param1, param2);
match ret9 {
    ()|table[] => {
        io:println("Call action successful");
        io:print("Student count with ID = 3: ");
        io:println(param1.value);
        io:print("Student count with ID = 2: ");
        io:println(param2.value);
    }
    error err => io:println("Stored procedure call failed: " + err.message);
}
```
