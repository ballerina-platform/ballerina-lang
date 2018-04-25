## Package overview

This package provides the functionality required to access and manipulate data stored in any type of relational database that is accessible via Java Database Connectivity (JDBC). 

### Endpoint 

To access a database, you must first create an `endpoint`, which is a virtual representation of the physical endpoint that you are trying to connect to. Create an endpoint of the JDBC client type (i.e., `jdbc:Client`) and provide the necessary connection parameters. This will create a pool of connections to the specified database. A sample for creating an endpoint with JDBC client can be found below. 

**NOTE**: Even though JDBC client type supports connecting to any type of relational database that is accessible via JDBC, if you are using a MySQL or H2 database, it is recommended to use endpoints that are created using the client types specific to them via the relevant ballerina packages. 

### Database operations

Once the endpoint is created, database operations can be executed through that endpoint. This package provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. Samples for these operations could be found below. Details of the SQL data types and query parameters relevant for these database operations could be found in the documentation of SQL package. 

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

Following is an example of creating a table with two columns. One field is of type int and the other of varchar. The CREATE statement is executed via the `update` operation of the endpoint.

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

In the following second example, parameter values which are in local variables are directly passed as parameters to the `update` operation. This direct parameter passing can be done for any primitive ballerina type like string, int, float, boolean. The sql type of the parameter is derived from the type of the ballerina variable passed in. 

```ballerina
int id = 2;
string name = "Anne";
var ret2 = testDB->update("INSERT INTO Students(StudentID, LastName) values (?, ?)", id, name);
match ret2 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

In the following third example, parameter values are passed as `sql:Parameter` to the `update` operation. This sql:Parameter needs to used when we need to provide more details like exact sql type of the parameter, and parameter direction etc. The default parameter direction is "IN". Refer sql package for more details on parameters.

```ballerina
sql:Parameter p1 = { sqlType: sql:TYPE_INTEGER, value: 3 };
sql:Parameter p2 = { sqlType: sql:TYPE_VARCHAR, value: "James" };
var ret3 = testDB->update("INSERT INTO Students(StudentID, LastName) values (?, ?)", p1, p2);
match ret3 {
    int retInt => io:println("Inserted row count to Students table: " + retInt);
    error err => io:println("Insert to Students table failed: " + err.message);
}
```

### Selecting data

Following is an example of selecting data. First, a type is created to represent the returned result set. Then the SELECT query is execute via the `select` operation of the endpoint by passing that result set type. Once the query is executed, each data record can be retrieved by looping the result set.

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
_ = testDB->call("{call InsertPersonData(100,'James')}", ());
testDB.stop();
```
In the next example, a stored procedure that does a data retrieval is called. It needs to pass a type for the returned result set. Similar to the executing a SELECT statement, data can be retrieved by iteratively reading the returned result set.

```ballerina
//Define a type to represent the results set.
type ResultCustomers {
    string FIRSTNAME,
};

table[] dts = check testDB->call("{call SelectPersonData()}", [ResultCustomers]);
string firstName;
while (dts[0].hasNext()) {
    ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
    firstName = rs.FIRSTNAME;
}
testDB.stop();
```

In the third example, a stored procedure performing a data retrieval is called, but this time it returns multiple result sets.

```ballerina
type ResultCustomers {
    string FIRSTNAME,
};

type ResultCustomers2 {
    string FIRSTNAME,
    string LASTNAME,
};

//call the stored procedure
table[] dts = check testDB->call("{call SelectPersonDataMultiple()}", [ResultCustomers, ResultCustomers2]);

string firstName1;
string firstName2;
string lastName;

//read the first result set
while (dts[0].hasNext()) {
    ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
    firstName1 = rs.FIRSTNAME;
}

//read the second result set
while (dts[1].hasNext()) {
    ResultCustomers2 rs = check <ResultCustomers2>dts[1].getNext();
    firstName2 = rs.FIRSTNAME;
    lastName = rs.LASTNAME;
}

testDB.stop();
```
## Package Contents



