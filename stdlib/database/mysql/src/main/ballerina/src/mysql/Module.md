## Module overview

This module provides the functionality required to access and manipulate data stored in MySQL database.  

**Prerequisite:** Please add the MySQL driver jar as a native library dependency in your Ballerina project. 
This module uses database properties from MySQL version 8.0.x onwards. Therefore, it is recommended to use a 
MySQL driver version greater than 8.0.x. Then, once you build the project with `ballerina build`
command, you should be able to run the resultant jar with, the `java -jar` command.

e.g. Ballerina.toml content.
Please change the path to the JDBC driver appropriately.

```toml
[project]
org-name= "sample"
version= "0.1.0"

[platform]
target = "java8"

    [[platform.libraries]]
    artafactId = "mysql-connector-java"
    version = "8.0.17"
    path = "/path/to/mysql-connector-java-8.0.17.jar"
    groupId = "mysql"
    modules = ["samplemodule"]
``` 

Or, if you're trying to run a single bal file, you can copy the JDBC driver into `${BALLERINA_HOME}/bre/lib` and 
run the bal file with `ballerina run` command.

### Client
To access a database, you must first create a 
[mysql:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/mysql/clients/Client.html) object. 
The examples for creating a MySQL client can be found below.

#### Creating a client
This example shows different ways of creating the `mysql:Client`. 

The client can be created with empty constructor, and hence the client will be initialized with default properties. 
The first example with `dbClient1` demonstrates this.

The `dbClient2` receives host, user and password. Since the properties are passed in the same order as it is defined 
in the `jdbc:Client` you can pass it without named params.

The `dbClient3` uses the named params to pass the attributes since it is skipping some params in the constructor. 
Further [mysql:Options](https://ballerina.io/learn/api-docs/ballerina/api-docs/mysql/records/Options.html) 
is passed to configure the SSL and connection timeout in the MySQL client. 

Similarly `dbClient4` uses the named params, and it provides a unshared connection pool in the type of 
[sql:ConnectionPool](https://ballerina.io/learn/api-docs/ballerina/api-docs/sql/records/ConnectionPool.html) 
to be used within the client. Please refer [SQL Module](https://ballerina.io/learn/api-docs/ballerina/sql/index.html) 
to get more details about connection pooling.

```ballerina
mysql:Client dbClient1 = new ();
mysql:Client dbClient2 = new ("localhost", "rootUser", "rooPass", 
                              "information_schema", 3306);
                              
mysql:Options mysqlOptions = {
  ssl: {
    mode: mysql:SSL_PREFERRED
  },
  connectTimeoutInSeconds: 10
};
mysql:Client dbClient3 = new (user = "rootUser", password = "rootPass",
                              options = mysqlOptions);
mysql:Client dbClient3 = new (user = "rootUser", password = "rootPass",
                              connectionPool = {maxOpenConnections: 5});
```
You can find more details about each property in 
[mysql:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/mysql/clients/Client.html) constructor. 

The [mysql:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/mysql/clients/Client.html) references 
[sql:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/sql/clients/Client.html) and hence all operations 
defined by the `sql:Client` will be supported by `mysql:Client` as well. 

Please refer [SQL Module](https://ballerina.io/learn/api-docs/ballerina/sql/index.html) for more information on 
all operations supported by `mysql:Client` which includes below. 

1. Connection Pooling
2. Querying data
3. Inserting data
4. Updating data
5. Deleting data
6. Batch insert and update data
7. Closing client