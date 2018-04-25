## Package overview

This package provides the functionality required to access and manipulate data stored in any type of relational database that is accessible via Java Database Connectivity (JDBC). 

### Endpoint 

To access a database, you must first create an `endpoint`, which is a virtual representation of the physical endpoint that you are trying to connect to. Create an endpoint of the JDBC client type (i.e., `jdbc:Client`) and provide the necessary connection parameters. This will create a pool of connections to the specified database. A sample for creating an endpoint with a JDBC client can be found below. 

**NOTE**: Even though JDBC client type supports connecting to any type of relational database that is accessible via JDBC, if you are using a MySQL or H2 database, it is recommended to use endpoints that are created using the client types specific to them via the relevant Ballerina packages. 

### Database operations

Once the endpoint is created, database operations can be executed through that endpoint. This package provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. Samples for these operations can be found below. Details of the SQL data types and query parameters relevant for these database operations can be found in the documentation for the SQL package. 

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
The full list of endpoint properties can be found in the `sql:PoolOptions` type.

### Creating tables

Following is an example of creating a table with two columns. One column is of type int and the other of varchar. The CREATE statement is executed via the `update` operation of the endpoint.

```ballerina
//Create ‘Students’ table with fields ‘StudentID’ and ‘LastName’.
var ret = testDB->update("CREATE TABLE IF NOT EXISTS Students(StudentID int, LastName varchar(255))");
match ret {
    int retInt => io:println("Students table create status in DB: " + retInt);
    error err => io:println("Students table create failed: " + err.message);
}
```

### Inserting data

Following are three examples of data insertion by executing an INSERT statement using the `update` operation of the endpoint. 

In the first example, query parameter values are directly passed into the query statement of the `update` operation:

```ballerina
var ret1 = testDB->update("INSERT INTO Students(StudentID, LastName) values (1, 'john')");
match ret1 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

In the next example, parameter values that are in local variables are directly passed as parameters to the `update` operation. This direct parameter passing can be done for any primitive Ballerina type like string, int, float or boolean. The sql type of the parameter is derived from the type of the Ballerina variable passed in. 

```ballerina
int id = 2;
string name = "Anne";
var ret2 = testDB->update("INSERT INTO Students(StudentID, LastName) values (?, ?)", id, name);
match ret2 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

In the next example, parameter values are passed as `sql:Parameter` to the `update` operation. Use sql:Parameter when you need to provide details such as the exact SQL type of the parameter or the parameter direction. The default parameter direction is "IN". Refer to the `sql` package for more details on parameters.

```ballerina
sql:Parameter p1 = { sqlType: sql:TYPE_INTEGER, value: 3 };
sql:Parameter p2 = { sqlType: sql:TYPE_VARCHAR, value: "James" };
var ret3 = testDB->update("INSERT INTO Students(StudentID, LastName) values (?, ?)", p1, p2);
match ret3 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

### Inserting with Returning generated keys

Following is an example of inserting data, while returning the auto generated keyes by executing an INSERT statement using the `updateWithGeneratedKeys` operation.

```ballerina
int age = 31;
string name = "Kate";
var ret0 = testDB->updateWithGeneratedKeys("INSERT INTO student(age, name) values (?, ?)", (), age, name);
match ret0 {
    (int, string[]) y => {
        var (count, ids) = y;
        io:println("Inserted row count: " + count);
        io:println("Generated key: " + ids[0]);
    }
    error err => io:println("Insert to student table failed: " + err.message);
}
```

### Selecting data

Following is an example of selecting data. First, a type is created to represent the returned result set. Then the SELECT query is executed via the `select` operation of the endpoint by passing that result set type. Once the query is executed, each data record can be retrieved by looping the result set. The table returned by the select operation holds a pointer to the actual data in the database and it loads data from the table only when it is accessed. This table can be iterated only once. 

```ballerina
//Define a type to represent the results set.
type Student {
    int id,
    string name,
};

//Select the data from table
var selectRet = testDB->select("SELECT * FROM Students WHERE StudentID = 1", Student);
table<Student> dt;
match selectRet {
    table tableReturned => dt = tableReturned;
    error err => io:println("Select data from Students table failed: " + err.message);
}

//Access the returned table
foreach record in dt {
    io:println("Student:Name:" + record.name);
}
```

If the same table needs to be re-iterated multiple times, loadToMemory named argument is set to `true` in `select` action.

```ballerina
var selectRet = testDB->select("SELECT * FROM student", Student, loadToMemory = true);
table<Student> dt;
match selectRet {
    table tableReturned => dt = tableReturned;
    error err => io:println("Select data from student table failed: " + err.message);
}

foreach record in dt {
    io:println("Student:" + record.id + "|" + record.name + "|" + record.age);
}
foreach record in dt {
    io:println("Student:" + record.id + "|" + record.name + "|" + record.age);
}
````


### Updating data

Following is an example of modifying data by executing an UPDATE statement via the `update` operation of the endpoint.

```ballerina
var ret4 = testDB->update("Update Students set LastName = 'Johnes' where StudentID = ?", 1);
match ret4 {
    int retInt => io:println("Updated row count in Students table: " + retInt);
    error err => io:println("Update in Students table failed: " + err.message);
}
```

### Batch updating data

Following example demonstrates how to insert multiple records with a single INSERT statement executed via the `batchUpdate` operation of the endpoint. This is done by first creating multiple parameter arrays, each representing a single record, and then providing those to the `batchUpdate` operation. Similarly, multiple UPDATE statements could be also executed via `batchUpdate`.

```ballerina
//Create the first batch of parameters
sql:Parameter para1 = { sqlType: sql:TYPE_INTEGER, value: 5 };
sql:Parameter para2 = { sqlType: sql:TYPE_VARCHAR, value: "Alex" };
sql:Parameter[] parameters1 = [para1, para2];

//Create the second batch of parameters
sql:Parameter para3 = { sqlType: sql:TYPE_INTEGER, value: 6 };
sql:Parameter para4 = { sqlType: sql:TYPE_VARCHAR, value: "Peter" };
sql:Parameter[] parameters2 = [para3, para4];

//Do the batch update by passing the batches.
var ret5 = testDB->batchUpdate("INSERT INTO Students(StudentID, LastName) values (?, ?)", parameters1, parameters2);
match ret5 {
    int[] counts => {
        io:println("Batch item 1 update counts: " + counts[0]);
        io:println("Batch item 2 update counts: " + counts[1]);
    }
    error err => io:println("Batch update action failed: " + err.message);
}
```

### Calling stored procedures

Following are three examples of executing stored procedures via the  `call` operation of the endpoint. 

In the first example, a simple stored procedure that inserts data are called:
```ballerina
//Create the stored procedure.
var ret6 = testDB->update("CREATE PROCEDURE INSERTDATA (IN pID INT, IN pName VARCHAR(255))
                           BEGIN
                              INSERT INTO Students(StudentID, LastName) values (pID, pName);
                           END");
match ret6 {
    int status => io:println("Stored proc creation status: " + status);
    error err => io:println("Stored procedure creation failed: " + err.message);
}

//Call the stored procedure.
var ret7 = testDB->call("{CALL INSERTDATA(?,?)}", (), 7, "George");
match ret7 {
    ()|table[] => io:println("Call action successful");
    error err => io:println("Stored procedure call failed: " + err.message);
}
```
In the next example, a stored procedure that accepts INOUT and OUT parameter is called.

```ballerina
//Create the stored procedure.
var ret8 = testDB->update("CREATE PROCEDURE GETCOUNT (INOUT pID INT, OUT pCount INT)
                           BEGIN
                                SELECT COUNT(*) INTO pID FROM Students WHERE StudentID = pID;
                                SELECT COUNT(*) INTO pCount FROM Students WHERE StudentID = 2;
                           END");
match ret8 {
    int status => io:println("Stored proc creation status: " + status);
    error err => io:println("Stored procedure creation failed: " + err.message);
}

//Call the stored procedure
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

### Get proxy table

A proxy for a database table that allows performing add/remove operations over the actual database table, can be obtained by `getProxyTable` action as follows.

```ballerina
var proxyRet = testDB->getProxyTable("student", Student);
table<Student> tbProxy;
match proxyRet {
    table tbReturned => tbProxy = tbReturned;
    error err => io:println("Proxy table retrieval failed: " + err.message);
}

// Iterate through the table and retrieve the data record corresponding to each row.
foreach record in tbProxy {
    io:println("Student:" + record.id + "|" + record.name + "|" + record.age);
}

// Data can be added to the database table through the proxied table.
Student s = { name: "Tim", age: 14 };
var addRet = tbProxy.add(s);
match addRet {
    () => io:println("Insertion to table successful");
    error err => io:println("Insertion to table failed: " + err.message);
}

// Data can be removed from the database table through the proxied table, by passing a
// function pointer which returns a boolean value evaluating whether a given record
// should be removed or not.
var rmRet = tbProxy.remove(isUnder20);
match rmRet {
    int count => io:println("Removed count: " + count);
    error err => io:println("Removing from table failed: " + err.message);
}
```



