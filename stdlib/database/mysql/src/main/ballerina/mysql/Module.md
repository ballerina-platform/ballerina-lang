## Module overview

This module provides the functionality required to access and manipulate data stored in a MySQL database. 

### Endpoint 

To access a database, you must first create a `client` object. Create a client of the MySQL Client type (i.e., `mysql:Client`) and provide the necessary connection parameters. A sample for creating a MySQL client can be found below.

### Connection pool handling

There are 3 possible scenarios of connection pool handling

1. If you do not provide the `poolOptions` field, a globally shareable pool will be created for your database if a connection pool matching with the properties you provided
doesn't exist already.

```ballerina
mysql:Client testDB = new({
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    dbOptions: { "useSSL": false }
});
```

2. If you define the `poolOptions` field inline, an unshareable connection pool will be created.

```ballerina
mysql:Client testDB = new({
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { "useSSL": false }
});
```

3. If you create a record of type `sql:PoolOptions` and reuse that in the configuration of multiple clients, for each
set of clients that connect to the same database instance with the same set of properties, a shared connection pool
will be created.

```ballerina
mysql:Client testDB1;
mysql:Client testDB2;
mysql:Client testDB3;

sql:PoolOptions poolOptions1 = { maximumPoolSize: 5 };

testDB1 = new({
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: poolOptions1,
    dbOptions: { "useSSL": false }
});

testDB2 = new({
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: poolOptions1,
    dbOptions: { "useSSL": false }
});

testDB3 = new({
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: poolOptions1,
    dbOptions: { "useSSL": false }
});
```

The default values for the connection pool properties can be found in the `sql:PoolOptions` type.

### Database operations

Once the client is created, database operations can be executed through that client. This module provides support for creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and batch updating data. For more details on the supported remote functions refer to the `sql` module. Also the details of the SQL data types and query parameters relevant to these database operations can be found in the documentation of the `sql` module.

## Samples

### Creating an endpoint
```ballerina
mysql:Client testDB = new({
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    dbOptions: { "useSSL": false }
});
```
The full list of client properties can be found in the `sql:PoolOptions` type.

