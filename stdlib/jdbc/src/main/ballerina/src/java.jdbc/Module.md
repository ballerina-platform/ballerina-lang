## Module overview

This module provides the functionality required to access and manipulate data stored in any type of relational database 
that is accessible via Java Database Connectivity (JDBC).

### Client

To access a database, you must first create a `Client` object. A sample for creating a JDBC client can be found below.

### Connection pool handling

There are 3 possible scenarios for connection pool handling.

1. Global, shareable default connection pool  
    If you do not provide the `poolOptions` field, a globally shareable pool will be created for your database unless
    a connection pool matching with the properties you provided already exists.

    ```ballerina
    jdbc:Client testDB = new({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "root",
        password: "root",
        dbOptions: { useSSL: false }
    });
    ```

2. Client owned, unsharable connection pool  
    If you define the `poolOptions` field inline, an unshareable connection pool will be created.

    ```ballerina
    jdbc:Client testDB = new({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "root",
        password: "root",
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { useSSL: false }
    });
    ```

3. Local shareable connection pool  
    If you create a record of type `jdbc:PoolOptions` and reuse that in the configuration of multiple clients, for each
    set of clients that connect to the same database instance with the same set of properties, a shared connection pool
    will be created.

    ```ballerina
    jdbc:Client testDB1;
    jdbc:Client testDB2;
    jdbc:Client testDB3;
    
    jdbc:PoolOptions poolOptions1 = { maximumPoolSize: 5 };
    
    testDB1 = new({
        url: "jdbc:mysql://localhost:3306/testdb1",
        username: "root",
        password: "root",
        poolOptions: poolOptions1,
        dbOptions: { useSSL: false }
    });
    
    testDB2 = new({
        url: "jdbc:mysql://localhost:3306/testdb1",
        username: "root",
        password: "root",
        poolOptions: poolOptions1,
        dbOptions: { useSSL: false }
    });
    
    testDB3 = new({
        url: "jdbc:mysql://localhost:3306/testdb2",
        username: "root",
        password: "root",
        poolOptions: poolOptions1,
        dbOptions: { useSSL: false }
    });
    ```
### Database operations

Once the client is created, database operations can be executed through that client. This module provides support for 
creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch 
updating data. Samples for these operations can be found below. 

## Samples

### Creating a client
```ballerina
jdbc:Client testDB = new({
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "root",
    password: "root",
    dbOptions: { useSSL: false }
});
```
The full list of client properties can be found listed under the `jdbc:PoolOptions` type.

### Creating tables

This sample creates a table with two columns. One column is of type `int`, and the other is of type `varchar`.
The CREATE statement is executed via the `update` remote function of the client.

```ballerina
// Create the ‘Students’ table with fields ‘id’, 'name' and ‘age’.
var returned = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT, age INT, 
                               name VARCHAR(255), PRIMARY KEY (id))");
if (returned is jdbc:UpdateResult) {
    io:println("Students table create status in DB: " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Students table creation failed: " + <string>err.detail()["message"]);
}
```

### Inserting data

This sample shows three examples of data insertion by executing an INSERT statement using the `update` remote function 
of the client.

In the first example, query parameter values are passed directly into the query statement of the `update`  remote function:

```ballerina
var returned = testDB->update("INSERT INTO student(age, name) values (23, 'john')");
if (returned is jdbc:UpdateResult) {
    io:println("Inserted row count to Students table: " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Insert to Students table failed: " + <string>err.detail()["message"]);
}
```

In the second example, the parameter values, which are in local variables, are passed directly as parameters to 
the `update` remote function. This direct parameter passing can be done for any primitive Ballerina type like `string`, 
`int`, `float`, or `boolean`. The sql type of the parameter is derived from the type of the Ballerina variable that 
is passed in.

```ballerina
string name = "Anne";
int age = 8;
var returned = testDB->update("INSERT INTO student(age, name) values (?, ?)", age, name);
if (returned is jdbc:UpdateResult) {
    io:println("Inserted row count to Students table: " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Insert to Students table failed: " + <string>err.detail()["message"]);
}
```

In the third example, parameter values are passed as an `jdbc:Parameter` to the `update` remote function. Use 
`jdbc:Parameter` when you need to provide more details such as the exact SQL type of the parameter, or the parameter 
direction. The default parameter direction is `IN`.

```ballerina
jdbc:Parameter p1 = { sqlType: jdbc:TYPE_VARCHAR, value: "James" };
jdbc:Parameter p2 = { sqlType: jdbc:TYPE_INTEGER, value: 10 };
var returned = testDB->update("INSERT INTO student(age, name) values (?, ?)", p2, p1);
if (returned is jdbc:UpdateResult) {
    io:println("Inserted row count to Students table: " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Insert to Students table failed: " + <string>err.detail()["message"]);
}
```

### Inserting data with auto-generated keys

This example demonstrates inserting data while returning the auto-generated keys. It achieves this by using the 
`update` remote function to execute the INSERT statement.

```ballerina
int age = 31;
string name = "Kate";
var retWithKey = testDB->update("INSERT INTO student (age, name) values (?, ?)", age, name);
if (retWithKey is jdbc:UpdateResult) {
    int count = retWithKey.updatedRowCount;
    int generatedKey = <int>retWithKey.generatedKeys["GENERATED_KEY"];
    io:println("Inserted row count: " + count);
    io:println("Generated key: " + generatedKey);
} else {
    error err = retWithKey;
    io:println("Insert to table failed: " + <string>err.detail()["message"]);
}
```

### Selecting data

This example demonstrates selecting data. First, a type is created to represent the returned result set. Next, the 
SELECT query is executed via the `select` remote function of the client by passing that result set type. Once the 
query is executed, each data record can be retrieved by looping the result set. The `table` returned by the select 
operation holds a pointer to the actual data in the database and it loads data from the table only when it is accessed. 
This table can be iterated only once.

```ballerina
// Define a type to represent the results set.
type Student record {
    int id;
    string name;
    int age;
};

// Select the data from the table.
var selectRet = testDB->select("SELECT * FROM student", Student);
if (selectRet is table<Student>) {
    // Iterating returned table.
    foreach var row in selectRet {
        io:println("Student:" + row.id + "|" + row.name + "|" + row.age);
    }
} else {
    error err = selectRet;
    io:println("Select data from student table failed: " + <string>err.detail()["message"]);
}
```

### Updating data

This example demonstrates modifying data by executing an UPDATE statement via the `update` remote function of 
the client.
```ballerina
var returned = testDB->update("UPDATE student SET name = 'Jones' WHERE age = ?", 23);
if (returned is jdbc:UpdateResult) {
    io:println("Updated row count in Students table: " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Insert to Students table failed: " + <string>err.detail()["message"]);
}
```

### Batch updating data

This example demonstrates how to insert multiple records with a single INSERT statement that is executed via the 
`batchUpdate` remote function of the client. This is done by first creating multiple parameter arrays, each 
representing a single record, and then passing those arrays to the `batchUpdate` operation. Similarly, multiple 
UPDATE statements can also be executed via `batchUpdate`.

```ballerina
// Create the first batch of parameters.
jdbc:Parameter para1 = { sqlType: jdbc:TYPE_VARCHAR, value: "Alex" };
jdbc:Parameter para2 = { sqlType: jdbc:TYPE_INTEGER, value: 12 };
jdbc:Parameter[] parameters1 = [para1, para2];

// Create the second batch of parameters.
jdbc:Parameter para3 = { sqlType: jdbc:TYPE_VARCHAR, value: "Peter" };
jdbc:Parameter para4 = { sqlType: jdbc:TYPE_INTEGER, value: 6 };
jdbc:Parameter[] parameters2 = [para3, para4];

// Do the batch update by passing the batches.
jdbc:BatchUpdateResult ret = testDB->batchUpdate("INSERT INTO Students(name, age) values (?, ?)", 
                                                 false, parameters1, parameters2);
error? e = ret.returnedError;
if (e is error) {
    io:println("Error occurred:" + <string>e.detail()["message"]);
} else {
    io:println("Batch item 1 update count: " + ret.updatedRowCount[0]);
    io:println("Batch item 2 update count: " + ret.updatedRowCount[1]);
}
```

### Calling stored procedures

The following examples demonstrate executing stored procedures via the `call` remote function of the client.

The first example shows how to create and call a simple stored procedure that inserts data.
```ballerina
// Create the stored procedure.
var returned = testDB->update("CREATE PROCEDURE INSERTDATA (IN pName VARCHAR(255), IN pAge INT)
                           BEGIN
                              INSERT INTO Students(name, age) values (pName, pAge);
                           END");
if (returned is jdbc:UpdateResult) {
    io:println("Stored proc creation status: : " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Stored proc creation failed: " + <string>err.detail()["message"]);
}

// Call the stored procedure.
var retCall = testDB->call("{CALL INSERTDATA(?,?)}", (), "George", 15);
if (retCall is ()|table<record {}>[]) {
    io:println("Call operation successful");
} else {
    error err = retCall;
    io:println("Stored procedure call failed: " + <string>err.detail()["message"]);
}
```
This next example shows how to create and call a stored procedure that accepts `INOUT` and `OUT` parameters.

```ballerina
// Create the stored procedure.
var returned = testDB->update("CREATE PROCEDURE GETCOUNT (INOUT pID INT, OUT pCount INT)
                           BEGIN
                                SELECT COUNT(*) INTO pID FROM Students WHERE id = pID;
                                SELECT COUNT(*) INTO pCount FROM Students WHERE id = 2;
                           END");
if (returned is jdbc:UpdateResult) {
    io:println("Stored proc creation status: : " + returned.updatedRowCount);
} else {
    error err = returned;
    io:println("Stored procedure creation failed:  " + <string>err.detail()["message"]);
}

// Call the stored procedure.
jdbc:Parameter param1 = { sqlType: jdbc:TYPE_INTEGER, value: 3, direction: jdbc:DIRECTION_INOUT };
jdbc:Parameter param2 = { sqlType: jdbc:TYPE_INTEGER, direction: jdbc:DIRECTION_OUT };
var retCall = testDB->call("{CALL GETCOUNT(?,?)}", (), param1, param2);
if (retCall is ()|table<record {}>[]) {
    io:println("Call operation successful");
    io:print("Student count with ID = 3: ");
    io:println(param1.value);
    io:print("Student count with ID = 2: ");
    io:println(param2.value);
} else {
    error err = retCall;
    io:println("Stored procedure call failed: " + <string>err.detail()["message"]);
}
```