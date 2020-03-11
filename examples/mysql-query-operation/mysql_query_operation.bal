import ballerina/io;
import ballerina/mysql;
import ballerina/sql;

//Username and password of MySQL database. This is used in below examples when initializing the
//MySQL connector. Please change these based on your setup if you want to try locally.
string dbUser = "root";
string dbPassword = "Test@123";

function simpleQuery(mysql:Client mysqlClient) {
    io:println("------ Start Simple Query -------");
    //Select the rows in the database table via query remote operation.
    // The result is returned as a stream, and the elements of the stream can
    // be either record or error.
    stream<record{}, error> resultStream = mysqlClient->query("Select * from Customers");

    //If there is any error during the execution of the sql query or iteration of the
    // result stream, the result stream will terminate and return the error.
    error? e = resultStream.forEach(function(record {} result) {
        io:println(result);
        io:print("Customer first name: ");
        io:println(result["FirstName"]);
        io:print("Customer last name: ");
        io:println(result["LastName"]);
    });
    //Check and handle the error during the sql query
    //or iteration of the result stream.
    if (e is error) {
        io:println("ForEach operation on the stream failed!");
        io:println(e);
    }

    //In general cases, the stream will be closed automatically
    // when the stream is fully consumed or any error is encountered. However, in
    //case if the stream is not fully consumed, stream should be closed specifically.
    e = resultStream.close();
    io:println("------ End Simple Query -------");
}

function countRows(mysql:Client mysqlClient) {
    io:println("------ Start Count Total Rows -------");
    //The result of the count operation is provided as record stream.
    stream<record{}, error> resultStream = mysqlClient->query("Select count(*) as Total from Customers");

    //Since the above count query will return only single row, next() operation is sufficient
    //to retrieve the data.
    record {|record {} value;|}|error? result = resultStream.next();

    //Check the result and retrieve the value for total.
    if (result is record {|record {} value;|}) {
        io:print("Total rows in customer table : ");
        io:println(result.value["Total"]);
    } else if (result is error) {
        io:println("Next operation on the stream failed!");
        io:println(result);
    } else {
        io:println("Customer table is empty");
    }
    //Close the stream.
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

function typedQuery(mysql:Client mysqlClient) {
    io:println("------ Start Query With Type Description -------");
    //The result is returned as a Customer record stream, and the elements
    //of the stream can be either Customer record or error.
    stream<record{}, error> resultStream = mysqlClient->query("Select * from Customers", Customer);

    //Cast to the generic record type to the Customer stream type.
    stream<Customer, sql:Error> customerStream = <stream<Customer, sql:Error>>resultStream;

    //Access customer information directly by using the defined record.
    error? e = customerStream.forEach(function(Customer customer) {
        io:println(customer);
    });

    //Check and handle the error during the query execution
    //or iteration of the result.
    if (e is error) {
        io:println("ForEach operation on the stream failed!");
        io:println(e);
    }

    //Close the stream.
    e = resultStream.close();
    io:println("------ End Query With Type Description -------");
}

//Initialize the database table with sample data.
function initializeTable() returns sql:Error? {
    mysql:Client mysqlClient = check new (user = dbUser, password = dbPassword);
    sql:ExecuteResult? result = check mysqlClient->execute("CREATE DATABASE IF NOT EXISTS MYSQL_BBE");
    result = check mysqlClient->execute("DROP TABLE IF EXISTS MYSQL_BBE.Customers");
    result = check mysqlClient->execute("CREATE TABLE IF NOT EXISTS MYSQL_BBE.Customers(customerId INTEGER " +
        "NOT NULL AUTO_INCREMENT, FirstName  VARCHAR(300), LastName  VARCHAR(300), RegistrationID INTEGER," +
        "CreditLimit DOUBLE, Country  VARCHAR(300), PRIMARY KEY (CustomerId))");
    result = check mysqlClient->execute("INSERT INTO MYSQL_BBE.Customers (FirstName,LastName,RegistrationID," +
        "CreditLimit,Country) VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA')");
    result = check mysqlClient->execute("INSERT INTO MYSQL_BBE.Customers (FirstName,LastName,RegistrationID," +
        "CreditLimit,Country) VALUES ('Dan', 'Brown', 2, 10000, 'UK')");
    check mysqlClient.close();
}

public function main() {
    //Initialize the MySQL client
    sql:Error? err = initializeTable();
    if (err is sql:Error) {
        io:println("Sample data initialization failed!");
        io:println(err);
    } else {
        mysql:Client|sql:Error mysqlClient = new (user = dbUser, password = dbPassword, database = "MYSQL_BBE");
        if (mysqlClient is mysql:Client) {
            //Executes the select queries in different options.
            simpleQuery(mysqlClient);
            countRows(mysqlClient);
            typedQuery(mysqlClient);
            io:println("Successfully queried the database!");

            //Close the MySQL client.
            sql:Error? e = mysqlClient.close();
        } else {
            io:println("MySQL Client initialization for querying data failed!!");
            io:println(mysqlClient);
        }
    }
}
