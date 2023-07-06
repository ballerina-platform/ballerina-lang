import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;

function simpleQuery(jdbc:Client jdbcClient) {
    io:println("------ Start Simple Query -------");
    // Select the rows in the database table via the query remote operation.
    // The result is returned as a stream and the elements of the stream can
    // be either a record or an error.
    stream<record{}, error?> resultStream =
        jdbcClient->query("Select * from Customers");

    // If there is any error during the execution of the SQL query or
    // iteration of the result stream, the result stream will terminate and
    // return the error.
    error? e = resultStream.forEach(function(record {} result) {
        io:println("Full Customer details: ", result);
        io:println("Customer first name: ", result["FIRSTNAME"]);
        io:println("Customer last name: ", result["LASTNAME"]);
    });
    // Check and handle the error during the SQL query
    // or iteration of the result stream.
    if (e is error) {
        io:println("ForEach operation on the stream failed!");
        io:println(e);
    }

    io:println("------ End Simple Query -------");
}

function countRows(jdbc:Client jdbcClient) {
    io:println("------ Start Count Total Rows -------");
    // The result of the count operation is provided as a record stream.
    stream<record{}, error?> resultStream =
        jdbcClient->query("Select count(*) as total from Customers");

    // Since the above count query will return only a single row,
    // the `next()` operation is sufficient to retrieve the data.
    record {|record {} value;|}|error? result = resultStream.next();

    // Check the result and retrieve the value for total.
    if (result is record {|record {} value;|}) {
        io:println("Total rows in customer table : ", result.value["TOTAL"]);
    } else if (result is error) {
        io:println("Next operation on the stream failed. ", result);
    } else {
        io:println("Customer table is empty");
    }

    // In general cases, the stream will be closed automatically
    // when the stream is fully consumed or any error is encountered.
    // However, in case if the stream is not fully consumed, the stream
    // should be closed specifically.
    error? e = resultStream.close();

    io:println("------ End Count Total Rows -------");
}

// Define a record to load the query result schema as shown below in the
// 'typedQuery' function. In this example, all columns of the customer table
// will be loaded. Therefore, the `Customer` record will be created with all
// the columns. The column name of the result and the defined field name of
// the record will be matched case insensitively.
type Customer record {|
    int customerId;
    string lastName;
    string firstName;
    int registrationId;
    float creditLimit;
    string country;
|};

function typedQuery(jdbc:Client jdbcClient) {
    io:println("------ Start Query With Type Description -------");
    // The result is returned as a Customer record stream and the elements
    // of the stream can be either a Customer record or an error.
    stream<record{}, error?> resultStream =
        jdbcClient->query("Select * from Customers", Customer);

    // Cast the generic record type to the Customer stream type.
    stream<Customer, sql:Error?> customerStream =
        <stream<Customer, sql:Error?>>resultStream;

    // Iterate the customer stream.
    error? e = customerStream.forEach(function(Customer customer) {
        io:println(customer);
    });
    if (e is error) {
        io:println(e);
    }

    io:println("------ End Query With Type Description -------");
}

//Initialize the database table with sample data.
function initializeTable(jdbc:Client jdbcClient) returns sql:Error? {
    sql:ExecutionResult result =
        check jdbcClient->execute("DROP TABLE IF EXISTS Customers");
    result = check jdbcClient->execute("CREATE TABLE IF NOT EXISTS Customers(" +
        "customerId INTEGER NOT NULL IDENTITY, firstName  VARCHAR(300)," +
        "lastName  VARCHAR(300), registrationID INTEGER, creditLimit DOUBLE," +
        "country  VARCHAR(300), PRIMARY KEY (customerId))");
    result = check jdbcClient->execute("INSERT INTO Customers (firstName," +
        "lastName,registrationID,creditLimit,country) VALUES ('Peter', " +
        "'Stuart', 1, 5000.75, 'USA')");
    result = check jdbcClient->execute("INSERT INTO Customers (firstName, " +
        "lastName,registrationID,creditLimit,country) VALUES ('Dan', 'Brown'," +
        "2, 10000, 'UK')");
}

public function main() {
    // Initialize the JDBC client.
    jdbc:Client|sql:Error jdbcClient = new ("jdbc:h2:file:./target/customers",
        "rootUser", "rootPass");

    if (jdbcClient is jdbc:Client) {
        sql:Error? err = initializeTable(jdbcClient);
        if (err is sql:Error) {
            io:println("Customer table initialization failed!", err);
        } else {
            // Execute the select queries in different options.
            simpleQuery(jdbcClient);
            countRows(jdbcClient);
            typedQuery(jdbcClient);

            io:println("Queried the database successfully!");
        }
        // Close the JDBC client.
        sql:Error? e = jdbcClient.close();

    } else {
        io:println("Initialization failed!!");
        io:println(jdbcClient);
    }
}
