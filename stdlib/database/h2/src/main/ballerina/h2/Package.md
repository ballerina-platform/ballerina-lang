## Package overview

This package provides the functionality required to access and manipulate data stored in an H2 database. 

### Endpoint 

To access a database, you must first create an `endpoint`, which is a virtual representation of the physical endpoint of the H2 database that you are trying to connect to. Create an endpoint of the H2 client type (i.e., `h2:Client`) and provide the necessary connection parameters. This will create a pool of connections to the given H2 database. A sample for creating an endpoint with an H2 client can be found below. 

### Database operations

Once the endpoint is created, database operations can be executed through that endpoint. This package provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. For more details on the supported actions, refer to the `sql` package. Also the details of the SQL data types and query parameters relevant to these database operations can be found in the documentation for the `sql` package.

## Samples

### Creating an endpoint
```ballerina
endpoint h2:Client testDB {
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 5 }
};
```
The full list of endpoint properties can be found in the `sql:PoolOptions` type.
