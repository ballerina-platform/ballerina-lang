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
    artafactId = "mysql-connector-java"
    version = "8.0.17"
    path = "/path/to/mysql-connector-java-8.0.17.jar"
    groupId = "mysql"
    modules = ["samplemodule"]
``` 

Or, if you're trying to run a single bal file, you can copy the JDBC driver into `${BALLERINA_HOME}/bre/lib` and 
run the bal file with `ballerina run` command.

### Client
To access a database, you must first create a `Client` object. The code for creating a JDBC client can be found below.

#### Creating a client
```ballerina
jdbc:Client dbClient = new ("jdbc:h2:~/path/to/database");
jdbc:Client dbClient1 = new ("jdbc:h2:~/path/to/database", "root", "root");
jdbc:Client dbClient2 = new (url =  "jdbc:h2:~/path/to/database", user = "root", password = "root",
                            options = {datasourceName: "org.h2.jdbcx.JdbcDataSource", 
                            connectionPool = {maxOpenConnections: 5});
```
The client can be created by passing JDBC URL which is an mandatory property, and all other fields are optional. You
can find more details about each property in`jdbc:Client` below. 