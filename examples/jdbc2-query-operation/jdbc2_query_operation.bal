import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;

function simpleQuery(jdbc:Client jdbcClient) {
    io:println("------ Start Simple Query -------");
    // Select the rows in the database table via query remote operation.
    // The result is returned as a stream, and the elements of the stream can
    // be either record or error.
    stream<record{}, error> resultStream = jdbcClient->query("Select * from Customers");

    // If there is any error during the execution of the sql query or iteration of the
    // result stream, the result stream will terminate and return the error.
    error? e = resultStream.forEach(function(record {} result) {
        io:println(result);
        io:print("Customer first name: ");
        io:println(result["FIRSTNAME"]);
        io:print("Customer last name: ");
        io:println(result["LASTNAME"]);
    });
    // Check and handle the error during the sql query
    // or iteration of the result stream.
    if (e is error) {
        io:println("ForEach operation on the stream failed!");
        io:println(e);
    }

    // In general cases, the stream will be closed automatically
    // when the stream is fully consumed or any error is encountered. However, in
    // case if the stream is not fully consumed, stream should be closed specifically.
    e = resultStream.close();
    io:println("------ End Simple Query -------");
}

function countRows(jdbc:Client jdbcClient) {
    io:println("------ Start Count Total Rows -------");
    // The result of the count operation is provided as record stream.
    stream<record{}, error> resultStream = jdbcClient->query("Select count(*) as total from Customers");

    // Since the above count query will return only single row, next() operation is sufficient
    // to retrieve the data.
    record {|record {} value;|}|error? result = resultStream.next();

    // Check the result and retrieve the value for total.
    if (result is record {|record {} value;|}) {
        io:print("Total rows in customer table : ");
        io:println(result.value["TOTAL"]);
    } else if (result is error) {
        io:println("Next operation on the stream failed!");
        io:println(result);
    } else {
        io:println("Customer table is empty");
    }
    // Close the stream.
    error? e = resultStream.close();
    io:println("------ End Count Total Rows -------");
}

//Define a record to load the query result schema as shown in function 'typedQuery' below.
//In this example, all columns of the customer table will be loaded,
//therefore creating `Customer` record with all columns. The result's column name
//and the defined field name of the record will be matched with case insensitively.
type Customer record {
    int customerId;
    string lastName;
    string firstName;
    int registrationId;
    float creditLimit;
    string country;
};

function typedQuery(jdbc:Client jdbcClient) {
    io:println("------ Start Query With Type Description -------");
    // The result is returned as a Customer record stream, and the elements
    // of the stream can be either Customer record or error.
    stream<record{}, error> resultStream = jdbcClient->query("Select * from Customers", Customer);

    // Cast to the generic record type to the Customer stream type.
    stream<Customer, sql:Error> customerStream = <stream<Customer, sql:Error>>resultStream;

    // Iterate through the customer stream.
    error? e = customerStream.forEach(function(Customer customer) {
        io:println(customer);
    });
    if (e is error) {
        io:println(e);
    }

    // Close the stream.
    e = resultStream.close();
    io:println("------ End Query With Type Description -------");
}

//Initialize the database table with sample data.
function initializeTable(jdbc:Client jdbcClient) returns sql:Error? {
    sql:ExecuteResult? result = check jdbcClient->execute("DROP TABLE IF EXISTS Customers");
    result = check jdbcClient->execute("CREATE TABLE IF NOT EXISTS Customers(customerId INTEGER " +
        "NOT NULL IDENTITY, firstName  VARCHAR(300),lastName  VARCHAR(300), registrationID INTEGER," +
        "creditLimit DOUBLE, country  VARCHAR(300), PRIMARY KEY (customerId))");
    result = check jdbcClient->execute("INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)" +
        "VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA')");
    result = check jdbcClient->execute("INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)" +
        "VALUES ('Dan', 'Brown', 2, 10000, 'UK')");
}

public function main() {
    // Initialize the JDBC client
    jdbc:Client|sql:Error jdbcClient = new ("jdbc:h2:file:./target/customers", "rootUser", "rootPass");
    if (jdbcClient is jdbc:Client) {
        sql:Error? err = initializeTable(jdbcClient);
        if (err is sql:Error) {
            io:println("Customer table initialization failed!");
            io:println(err);
        } else {
            // Executes the select queries in different options.
            simpleQuery(jdbcClient);
            countRows(jdbcClient);
            typedQuery(jdbcClient);
            io:println("Successfully queried the database!");
        }
        // Close the JDBC client.
        sql:Error? e = jdbcClient.close();
    } else {
        io:println("Initialization failed!!");
        io:println(jdbcClient);
    }
}
