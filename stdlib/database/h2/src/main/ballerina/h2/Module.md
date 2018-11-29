## Module overview

This module provides the functionality required to access and manipulate data stored in an H2 database. 

### Endpoint 

To access a database, you must first create a `client` object. Create a client of the H2 Client type (i.e., `h2:Client`) and provide the necessary connection parameters. This will create a pool of connections to the given H2 database. A sample for creating a H2 client can be found below.

### Database operations

Once the client is created, database operations can be executed through that client. This module provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. For more details on the supported actions refer to the `sql` module. Also the details of the SQL data types and query parameters relevant to these database operations can be found in the documentation of the `sql` module.

## Samples

### Creating an endpoint
```ballerina
h2:Client testDB = new({
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 5 }
});
```
The full list of client properties can be found in the `sql:PoolOptions` type.
