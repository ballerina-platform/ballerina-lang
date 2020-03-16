import ballerina/io;
import ballerina/mysql;
import ballerina/sql;

// Username and password of the MySQL database. This is used in the below
// examples when initializing the MySQL connector. You need to change these
// based on your setup to try locally.
string dbUser = "root";
string dbPassword = "Test@123";

function initializeClients() returns sql:Error? {
    // Initialize the MySQL Client without any parameters. In that case,
    // all parameters will be using the default values:`localhost` for host,
    // `3306` for port, and `()` for user, password, and database.
    mysql:Client|sql:Error mysqlClient1 = new ();
    if (mysqlClient1 is sql:Error) {
        io:println("Error when initializing MySQL client without any params");
        io:println(mysqlClient1);
    } else {
        io:println("Simple MySQL client created successfully");
        check mysqlClient1.close();
    }

    // Initialize the MySQL client by providing the username and password.
    mysql:Client mysqlClient2 = check new ("localhost", dbUser, dbPassword);
    io:println("MySQL client with user and password created.");

    // Initialize the MySQL client by providing the username, password,
    // and default host.
    mysql:Client mysqlClient3 = check new (user = dbUser,
        password = dbPassword);
    io:println("MySQL client with user and password created " +
        "with default host.");

    // Initialize the MySQL client by providing the host, username,
    // password, database, and port.
    mysql:Client mysqlClient4 = check new ("localhost", dbUser, dbPassword,
        "information_schema", 3306);
    io:println("MySQL client with host, user, password, database and " +
        "port created.");

    // Initialize the MySQL client by providing additional
    // MySQL database properties.
    mysql:Options mysqlOptions = {
        // SSL is enabled by default and the default mode is
        // `sql:SSL_PREFERRED`. SSL will be disabled, if `ssl` is assigned to
        // `()`.
        ssl: {
            // Possible options for mode are `sql:SSL_PREFERRED`,
            // `sql:SSL_REQUIRED`, `sql:SSL_VERIFY_CERT`, and
            // `sql:SSL_VERIFY_IDENTITY`. For details on each mode, go to
            // the MySQL reference (https://dev.mysql.com/doc/refman/8.0/en/using-encrypted-connections.html).
            mode: mysql:SSL_PREFERRED
        },
        connectTimeoutInSeconds: 10
    };
    // Initialize the MySQL client with MySQL database options.
    mysql:Client mysqlClient5 = check new (user = dbUser, password = dbPassword,
    options = mysqlOptions);
    io:println("MySQL client with database options created.");

    // Connection pool is used to share and use the database connections
    // efficiently. In the above samples, the global connection pool is
    // created and shared among all the database clients since the
    // `connectionPool` property is not set.
    sql:ConnectionPool connPool = {
        // Default max number of open connections in the connection pool is 15.
        maxOpenConnections: 5,
        // Default max life time of a connection in the connection pool is
        // 1800 seconds (30 minutes).
        maxConnectionLifeTimeInSeconds: 2000.0,
        // Default minimum number of idle connections is 15.
        minIdleConnections: 5
    };

    // Initialize the MySQL client with the specific connection pool.
    mysql:Client mysqlClient6 = check new (user = dbUser, password = dbPassword,
        options = mysqlOptions, connectionPool = connPool);
    io:println("MySQL client with connection pool created.");

    // Initialize the MySQL client with all the parameters.
    mysql:Client mysqlClient7 = check new ("localhost", dbUser, dbPassword,
        "information_schema", 3306, mysqlOptions, connPool);

    // All properties are optional.
    // Hence, named attributes can be used specifically to assign
    // the attributes.
    mysql:Client mysqlClient8 = check new (host = "localhost",
        user = dbUser, password = dbPassword, database =
        "information_schema", port = 3306, options = mysqlOptions,
        connectionPool = connPool);

    // Close the clients to release the resource
    // and destroy the connection pool.
    check mysqlClient2.close();
    check mysqlClient3.close();
    check mysqlClient4.close();
    check mysqlClient5.close();
    check mysqlClient6.close();
    check mysqlClient7.close();
    check mysqlClient8.close();
}

//Initializes MySQL clients with different options.
public function main() {
    sql:Error? err = initializeClients();
    if (err is sql:Error) {
        io:println("Error occured, initialization failed! ", err);
    } else {
        io:println("Successfully completed initialization!");
    }
}
