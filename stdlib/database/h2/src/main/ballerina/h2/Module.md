## Module overview

This module provides the functionality that is required to access and manipulate the data stored in an H2 database. 

### Creating the client

You need to create a `client` object to access a database. You can create a sample H2 client as follows.

### Handling the connection pool

There are 3 possible usage scenarios for a connection pool.

**1. The globally shareable connection pool**

If you do not provide the `poolOptions` field, a globally shareable pool will be created for your database, unless
a connection pool matching the properties you provided already exists.

>**Info:** This is the connection pool that is used by default.

```ballerina
h2:Client testDB = new({
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: ""
});
```

**2. An unshareable connection pool owned by the client**

If you define the `poolOptions` field inline, an unshareable connection pool will be created.

```ballerina
h2:Client testDB = new({
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 5 }
});
```

**3. A locally shareable connection pool**

If you create a record of the `sql:PoolOptions` type and reuse that in the configuration of multiple clients, a shared
connection pool will be created, for each set of clients that connect to the same database instance with the same set
of properties.

```ballerina
h2:Client testDB1;
h2:Client testDB2;
h2:Client testDB3;

sql:PoolOptions poolOptions1 = { maximumPoolSize: 5 };

testDB1 = new({
    path: "/home/ballerina/test",
    name: "testdb1",
    username: "SA",
    password: "",
    poolOptions: poolOptions1
});

testDB2 = new({
    path: "/home/ballerina/test",
    name: "testdb2",
    username: "SA",
    password: "",
    poolOptions: poolOptions1
});

testDB3 = new({
    path: "/home/ballerina/test",
    name: "testdb1",
    username: "SA",
    password: "",
    poolOptions: poolOptions1
});
```

For the default values of the connection pool properties, see the documentation of the `sql:PoolOptions` type.

### Handling database operations

Once the client is created, the database operations can be executed through that client. This module provides support for
creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating a single data record, and updating data in batches. 

For more details on the supported remote functions, and of the SQL data types and query parameters relevant to these database operations, see the documentation of the `sql` module. 

## Samples

### Creating a client in the embedded mode of H2

```ballerina
h2:Client testDB = new({
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: ""
});
```

### Creating a client in the server mode of H2

```ballerina
h2:Client testDB = new({
    host: "localhost",
    port: 9092,
    name: "testdb",
    username: "SA",
    password: ""
});
```

### Creating a client in the in-memory mode of H2

```ballerina
h2:Client testDB = new({
    name: "testdb",
    username: "SA",
    password: ""
});
```

For a complete list of client properties, see the documentation of the `sql:PoolOptions` type.
