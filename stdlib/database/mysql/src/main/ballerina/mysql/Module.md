## Module overview

This module provides the functionality that is required to access and manipulate the data stored in a MySQL database. 

### Creating the client

You need to create a `client` object to access a database. You can create a sample MySQL client as follows.

### Handling the connection pool

There are 3 possible usage scenarios for a connection pool.

**1. The globally shareable connection pool**

If you do not provide the `poolOptions` field, a globally shareable pool will be created for your database unless
a connection pool matching the properties you provided already exists.

>**Info:** This is the connection pool that is used by default.

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

**2. An unshareable connection pool owned by the client**

If you define the `poolOptions` field inline, an unshareable connection pool will be created.

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

**3. A locally shareable connection pool**

If you create a record of the `sql:PoolOptions` type and reuse that in the configuration of multiple clients, a shared
connection pool will be created, for each set of clients that connect to the same database instance with the same set of
properties.

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

For the default values of the connection pool properties, see the documentation of the `sql:PoolOptions` type.

### Database operations

Once the client is created, the database operations can be executed through that client. This module provides support for
creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating a single data record, and updating data in batches. 

For more details on the supported remote functions, and of the details of the SQL data types and query parameters relevant to these database operations,see the documentation of the `sql` module.

## Samples

### Creating a client
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
For a complete list of client properties, see the documentation of the `sql:PoolOptions` type.

