## Package overview

This package provides the functionality required to access and manipulate data stored in any type of relational database that is accessible via Java Database Connectivity (JDBC). 

### Endpoint 

To access a database, you must first create an `Endpoint`, which is a virtual representation of the physical endpoint that you are trying to connect to. Create an endpoint of the JDBC client type (i.e., `jdbc:Client`) and provide the necessary connection parameters. This will create a pool of connections to the specified database. A sample for creating an endpoint with JDBC client can be found below. 

**NOTE**: Even though JDBC client type supports connecting to any type of relational database that is accessible via JDBC, if you are using a MySQL or H2 database, it is recommended to use endpoints that are created using the client types specific to them via the relevant ballerina packages. 

### Database operations

Once the endpoint is created, database operations can be executed through that endpoint. This package provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. Samples for these operations could be found below. Details of the SQL data types and query parameters relevant for these database operations could be found in the documentation of SQL package. Once the operation is completed, `stop` function must be called to terminate the connection pool of the endpoint. 

## Samples

### Creating an endpoint
```ballerina
endpoint jdbc:Client testDB {
    url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
    username:"SA",
    poolOptions:{maximumPoolSize:1}
};
```
The full list of endpoint properties can be found in the `sql:PoolOptions` type.

### Creating tables

Following is an example of creating a table with two fields. One field is of type int and the other of varchar. The CREATE statement is executed via the `update` operation of the endpoint.

```ballerina
//Create ‘Students’ table with fields ‘StudentID’ and ‘LastName’. 
int returnValue = check testDB->update("CREATE TABLE IF NOT EXISTS Students(StudentID int, LastName varchar(255))");

//Terminate the connection pool.
testDB.stop();
```

### Selecting data

Following is an example of selecting data. First, a type is created to represent the returned result set. Then the SELECT query is execute via the `select` operation of the endpoint by passing that result set type. Once the query is executed, each data record can be retrieved by looping the result set.

```ballerina
//Define a type to represent the results set.
type ResultCustomers {
    string FIRSTNAME,
};

//Retrieve the FirstName from the Customers table by providing the registrationID.
table dt = check testDB->select("SELECT  FirstName from Customers where registrationID = 1", ResultCustomers);
string firstName;

//Iterate the results-set and retrieve the first name.
while (dt.hasNext()) {
    ResultCustomers rs = check <ResultCustomers>dt.getNext();
    firstName = rs.FIRSTNAME;
}
testDB.stop();
```

### Inserting data

Following are two examples of data insertion by executing an INSERT statement using the `update` operation of the endpoint. 

In the first example, query parameter values are directly passed into the `update` operation:

```ballerina
//Insert data into Customers table
var insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                   values ('James', 'Clerk', 2, 5000.75, 'USA')");
testDB.stop();
```
In the following second example, parameter values are first assigned to local variables of type `sql:Parameter` and then passed into the `update` operation.

```ballerina
string s1 = "Anne";
sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:s1, direction:sql:DIRECTION_IN};
sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"James", direction:sql:DIRECTION_IN};
sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:3, direction:sql:DIRECTION_IN};
sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:5000.75, direction:sql:DIRECTION_IN};
sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"UK", direction:sql:DIRECTION_IN};

//Insert data into Customers table
int insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                 values (?,?,?,?,?)", para1, para2, para3, para4, para5);
testDB.stop();
```

### Updating data

Following is an example of modifying data by executing an UPDATE statement via the `update` operation of the endpoint.

```ballerina
var updateCount = check testDB->update("Update Customers set country = 'UK' where registrationID = 1");
testDB.stop();
```

### Batch updating data

Following example demonstrates how to insert multiple records with a single INSERT statement executed via the `batchUpdate` operation of the endpoint. This is done by first creating multiple parameter arrays, each representing a single record, and then providing those to the `batchUpdate` operation. Similarly, multiple UPDATE statements could be also executed via `batchUpdate`.

```ballerina
//Create the first batch of parameters
sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:20};
sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"USA"};
sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

//Create the second batch of parameters
para1 = {sqlType:sql:TYPE_VARCHAR, value:"John"};
para2 = {sqlType:sql:TYPE_VARCHAR, value:"Wick"};
para3 = {sqlType:sql:TYPE_INTEGER, value:30};
para4 = {sqlType:sql:TYPE_DOUBLE, value:8000.5};
para5 = {sqlType:sql:TYPE_VARCHAR, value:"India"};
sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];

//Do the batch update by passing the multiple parameter arrays
int[] updateCount = check testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                 values (?,?,?,?,?)", parameters1, parameters2);
testDB.stop();
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



