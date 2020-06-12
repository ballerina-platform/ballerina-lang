## Module overview

This module provides the functionality required to access and manipulate data stored in any type of relational database 
that is accessible via Java Database Connectivity (JDBC). 

**Prerequisite:** Please add the JDBC driver corresponding to the database you are trying to interact with, 
as a native library dependency in your Ballerina project. Then, once you build the project with `ballerina build`
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
    artafactId = "h2"
    version = "1.4.200"
    path = "/path/to/com.h2database.h2-1.4.200.jar"
    groupId = "com.h2database"
    modules = ["samplemodule"]
``` 

Or, if you're trying to run a single bal file, you can copy the JDBC driver into `${BALLERINA_HOME}/bre/lib` and 
run the bal file with `ballerina run` command.

### Client
To access a database, you must first create a 
[jdbc:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/java.jdbc/clients/Client.html) object. 
The examples for creating a JDBC client can be found below.

#### Creating a client
This example shows different ways of creating the `jdbc:Client`. The client can be created by passing 
JDBC URL which is an mandatory property, and all other fields are optional. 

The `dbClient1` receives only database URL, and the `dbClient2` receives user and password in addition to URL. 
If the properties are passed in the same order as it is defined in the `jdbc:Client` you can pass it 
without named params.

The `dbClient3` uses the named params to pass all the attributes, and provides `options` property in the type of 
[jdbc:Options](https://ballerina.io/learn/api-docs/ballerina/api-docs/java.jdbc/records/Options.html) 
and also uses the unshared connection pool in the type of 
[sql:ConnectionPool](https://ballerina.io/learn/api-docs/ballerina/api-docs/sql/records/ConnectionPool.html). 
Please refer [SQL Module](https://ballerina.io/learn/api-docs/ballerina/sql/index.html) to get more details 
about connection pooling.

The `dbClient4` receives some custom properties within the 
[jdbc:Options](https://ballerina.io/learn/api-docs/ballerina/api-docs/java.jdbc/records/Options.html), 
and those properties will be used by the `datasourceName` defined. 
As per the provided example, the datasource `org.h2.jdbcx.JdbcDataSource` will be configured with `loginTimeout` 
of `2000` milli seconds.

```ballerina
jdbc:Client dbClient1 = new ("jdbc:h2:~/path/to/database");
jdbc:Client dbClient2 = new ("jdbc:h2:~/path/to/database", "root", "root");
jdbc:Client dbClient3 = new (url =  "jdbc:h2:~/path/to/database",
                             user = "root", password = "root",
                             options = {
                                 datasourceName: "org.h2.jdbcx.JdbcDataSource"
                             },
                             connectionPool = {
                                 maxOpenConnections: 5
                             });
jdbc:Client dbClient4 = new (url =  "jdbc:h2:~/path/to/database", 
                             user = "root", password = "root",
                             options = {
                                datasourceName: "org.h2.jdbcx.JdbcDataSource", 
                                properties: {"loginTimeout": "2000"}
                             });                          
```

You can find more details about each property in 
[jdbc:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/java.jdbc/clients/Client.html) constructor. 

The [jdbc:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/java.jdbc/clients/Client.html) references 
[sql:Client](https://ballerina.io/learn/api-docs/ballerina/api-docs/sql/clients/Client.html) and 
hence all operations defined by the `sql:Client` will be supported by `jdbc:Client` as well. 

Please refer [SQL Module](https://ballerina.io/learn/api-docs/ballerina/sql/index.html) for more information on 
all operations supported by `jdbc:Client` which includes below. 

1. Connection Pooling
2. Querying data
3. Inserting data
4. Updating data
5. Deleting data
6. Batch insert and update data
7. Closing client