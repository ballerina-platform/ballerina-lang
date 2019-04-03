## Module overview

This module provides the functionality required to access and manipulate data stored in an H2 database. 

### Creating the client

You need to create a `client` object to access a database. The following is a sample for creating an H2 client.

### Handling the connection pool

There are 3 possible usage scenarios for a connection pool.

**1. Globally shareable connection pool**

If you do not provide the `poolOptions` field, a globally shareable pool will be created for your database unless
a connection pool matching the properties you provided already exists.

```ballerina
h2:Client testDB = new({
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: ""
});
```

**2. Unshareable connection pool owned by the client**

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

**3. Locally shareable connection pool**

If you create a record of the `sql:PoolOptions` type and reuse that in the configuration of multiple clients, a shared
connection pool will be created for each set of clients that connect to the same database instance with the same set
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

The default values of the connection pool properties can be found in the documentation of the `sql:PoolOptions` type.

### Database operations

Once the client is created, the database operations can be executed through that client. This module provides support for
creating tables and executing stored procedures. It also supports selecting, inserting, deleting, updating, and updating
data in batches. For more details on the supported remote functions refer to the `sql` module.
Also the details of the SQL data types and query parameters relevant to these database operations can be found in the
documentation of the `sql` module.

## Samples

### Creating a client in H2 Embedded Mode

```ballerina
h2:Client testDB = new({
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: ""
});
```

### Creating a client in H2 Server Mode

```ballerina
h2:Client testDB = new({
    host: "localhost",
    port: 9092,
    name: "testdb",
    username: "SA",
    password: ""
});
```

### Creating a client in H2 In-Memory Mode

```ballerina
h2:Client testDB = new({
    name: "testdb",
    username: "SA",
    password: ""
});
```

The full list of client properties can be found in the `sql:PoolOptions` type.
