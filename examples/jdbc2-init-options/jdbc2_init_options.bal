import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;

function initializeClients() returns sql:Error? {
    // Simple JDBC Client for H2 database with the mandatory
    // field - JDBC url. This client can be used with any database
    // by providing the corresponding JDBC URL and placing the relevant database
    // driver jar.
    jdbc:Client jdbcClient1 = check new ("jdbc:h2:file:./target/sample1");
    io:println("Simple JDBC client created.");

    // Initialize the JDBC client along with username and password.
    jdbc:Client jdbcClient2 = check new ("jdbc:h2:file:./target/sample2", "rootUser", "rootPass");
    io:println("JDBC client with user/password created.");

    // Initialize the JDBC client along with some additional
    // database properties. The database properties can differ based
    // on the specific JDBC datasource implementation of the database.
    jdbc:Options h2Options = {
        datasourceName: "org.h2.jdbcx.JdbcDataSource",
        properties: {"loginTimeout": "2000"}
    };
    jdbc:Client jdbcClient3 = check new ("jdbc:h2:file:./target/sample3", "rootUser", "rootPass", h2Options);
    io:println("JDBC client with database options created.");

    // Connection pool is used to share use the connections efficiently to the database.
    // In the above samples, the global connection pool is created, and shared
    // among all database clients since the `connectionPool` property is not set.
    sql:ConnectionPool connPool = {
        // Default max number of open connections in the connection pool is 15.
        maxOpenConnections: 5,
        // Default max life time of a connection in the connection pool is
        // 1800 seconds (30 minutes).
        maxConnectionLifeTimeInSeconds: 2000.0,
        // Default minimum number of idle connections is 15.
        minIdleConnections: 5
    };

    // Initialize the JDBC client with the specific connection pool.
    jdbc:Client jdbcClient4 = check new ("jdbc:h2:file:./target/sample4", "rootUser", "rootPass", h2Options, connPool);
    io:println("JDBC client with connection pool created.");

    // Other than JDBC URL, all properties are optional.
    // And hence named attributes can be used to specifically assign the attributes.
    jdbc:Client jdbcClient5 = check new (url = "jdbc:h2:file:./target/sample5", user = "rootUser",
        password = "rootPass", options = h2Options, connectionPool = connPool);

    // Initialize JDBC Client with URL and connection pool only.
    jdbc:Client jdbcClient6 = check new (url = "jdbc:h2:file:./target/sample6", connectionPool = connPool);
    io:println("JDBC client with optional params created.");

    // Close the clients to release the resource
    // and destroy the underneath connection pool.
    check jdbcClient1.close();
    check jdbcClient2.close();
    check jdbcClient3.close();
    check jdbcClient4.close();
    check jdbcClient5.close();
    check jdbcClient6.close();
}

//Initializes JDBC clients with different options.
public function main() {
    sql:Error? err = initializeClients();
    if (err is sql:Error) {
        io:println("Error occured, initialization failed!");
        io:println(err);
    } else {
        io:println("Successfully completed initialization!");
    }
}
