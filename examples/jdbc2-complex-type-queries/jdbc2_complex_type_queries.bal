import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;
import ballerina/time;

type BinaryType record {|
    int row_id;
    byte[] blob_type;
    string clob_type;
    byte[] binary_type;
|};

type ArrayType record {|
    int row_id;
    int[] int_array;
    int[] long_array;
    decimal[] float_array;
    float[] double_array;
    boolean[] boolean_array;
    string[] string_array;
|};

type DateTimeType record {|
    int row_id;
    string date_type;
    int time_type;
    time:Time timestamp_type;
    string datetime_type;
|};

function queryBinaryType(jdbc:Client jdbcClient) {
    io:println("------ Query Binary Type -------");
    // Select the rows with binary data types.
    // The name and type of the attributes within the record from the
    // `resultStream` will be automatically identified based on the column
    // name and type of the query result.
    stream<record{}, error> resultStream =
        jdbcClient->query("Select * from BINARY_TYPES");

    io:println("Result 1:");
    // If there is any error during the execution of the SQL query or
    // iteration of the result stream, the result stream will terminate and
    // return the error.
    error? e = resultStream.forEach(function(record {} result) {
        io:println(result);
    });
    if (e is error) {
        io:println(e);
    }
    // Since the `rowType` is provided as `BinaryType`, the `resultStream` will
    // have `BinaryType` records.
    resultStream = jdbcClient->query("Select * from BINARY_TYPES", BinaryType);
    stream<BinaryType, sql:Error> binaryResultStream
    = <stream<BinaryType, sql:Error>>resultStream;

    io:println("Result 2:");
    // Iterate the `binaryResultStream`.
    e = binaryResultStream.forEach(function(BinaryType result) {
        io:println(result);
    });
    if (e is error) {
        io:println(e);
    }
    io:println("------ ********* -------");
}


function queryArrayType(jdbc:Client jdbcClient) {
    io:println("------ Query Array Type -------");
    // Select the rows with array data types.
    // The name and type of the attributes within the record from the `
    // resultStream` will be automatically identified based on the column
    // name and type of the query result.
    stream<record{}, error> resultStream =
        jdbcClient->query("Select * from ARRAY_TYPES");

    io:println("Result 1:");
    // If there is any error during the execution of the SQL query or
    // iteration of the result stream, the result stream will terminate and
    // return the error.
    error? e = resultStream.forEach(function(record {} result) {
        io:println(result);
    });
    if (e is error) {
        io:println(e);
    }
    // Since the `rowType` is provided as `ArrayType`, the `resultStream` will
    // have `ArrayType` records.
    resultStream = jdbcClient->query("Select * from ARRAY_TYPES", ArrayType);
    stream<ArrayType, sql:Error> arrayResultStream =
        <stream<ArrayType, sql:Error>>resultStream;

    io:println("Result 2:");
    // Iterate the `arrayResultStream`.
    e = arrayResultStream.forEach(function(ArrayType result) {
        io:println(result);
    });
    if (e is error) {
        io:println(e);
    }
    io:println("------ ********* -------");
}

function queryDateTimeType(jdbc:Client jdbcClient) {
    io:println("------ Query Date Time Type -------");
    // Select the rows with date/time data types.
    // The name and type of the attributes within the record from
    // the `resultStream` will be automatically identified based on the
    // column name and type of the query result.
    stream<record{}, error> resultStream =
        jdbcClient->query("Select * from DATE_TIME_TYPES");

    io:println("Result 1:");
    // If there is any error during the execution of the SQL query or
    // iteration of the result stream, the result stream will terminate and
    // return the error.
    error? e = resultStream.forEach(function(record {} result) {
        io:println(result);
    });
    if (e is error) {
        io:println(e);
    }
    // Since the `rowType` is provided as `DateTimeType`, the `resultStream`
    // will have `DateTimeType` records. The Date, Time, DateTime, and
    // Timestamp fields of the database tabl can be mapped to time:Time,
    // string, and int types in Ballerina.
    resultStream = jdbcClient->query("Select * from DATE_TIME_TYPES",
        DateTimeType);
    stream<DateTimeType, sql:Error> dateResultStream =
        <stream<DateTimeType, sql:Error>>resultStream;

    io:println("Result 2:");
    // Iterate the `dateResultStream`.
    e = dateResultStream.forEach(function(DateTimeType result) {
        io:println(result);
    });
    if (e is error) {
        io:println(e);
    }
    io:println("------ ********* -------");
}

//Initialize the database table with sample data.
function initializeTable(jdbc:Client jdbcClient) returns sql:Error? {
    sql:ExecuteResult? result =
        check jdbcClient->execute("DROP TABLE IF EXISTS BINARY_TYPES");
    result = check jdbcClient->execute("CREATE TABLE BINARY_TYPES (row_id " +
        "INTEGER NOT NULL, blob_type BLOB(1024), clob_type CLOB(1024)," +
        "binary_type BINARY(27), PRIMARY KEY (row_id))");
    result = check jdbcClient->execute("INSERT INTO BINARY_TYPES (row_id," +
        "blob_type, clob_type, binary_type) VALUES (1, " +
        "X'77736F322062616C6C6572696E6120626C6F6220746573742E', CONVERT" +
        "('very long text', CLOB)," +
        "X'77736F322062616C6C6572696E612062696E61727920746573742E')");

    result = check jdbcClient->execute("DROP TABLE IF EXISTS ARRAY_TYPES");
    result = check jdbcClient->execute("CREATE TABLE ARRAY_TYPES (row_id " +
        "INTEGER NOT NULL, int_array ARRAY, long_array ARRAY,float_array " +
        "ARRAY, double_array ARRAY, boolean_array ARRAY, string_array ARRAY," +
        "PRIMARY KEY (row_id))");
    result = check jdbcClient->execute("INSERT INTO ARRAY_TYPES (row_id, " +
        "int_array, long_array, float_array, double_array, boolean_array, " +
        "string_array) VALUES (1, (1, 2, 3), (100000000, 200000000, " +
        "300000000), (245.23, 5559.49, 8796.123), (245.23, 5559.49, " +
        "8796.123), (TRUE, FALSE, TRUE), ('Hello', 'Ballerina'))");

    result = check jdbcClient->execute("DROP TABLE IF EXISTS DATE_TIME_TYPES");
    result = check jdbcClient->execute("CREATE TABLE DATE_TIME_TYPES(row_id " +
        " INTEGER NOT NULL, date_type DATE, time_type TIME, timestamp_type " +
        "timestamp, datetime_type  datetime, PRIMARY KEY (row_id))");
    result = check jdbcClient->execute("Insert into DATE_TIME_TYPES (row_id," +
        " date_type, time_type, timestamp_type, datetime_type) values (1," +
        "'2017-05-23','14:15:23','2017-01-25 16:33:55','2017-01-25 16:33:55')");
}

public function main() {
    // Initialize the JDBC client.
    jdbc:Client|sql:Error jdbcClient = new ("jdbc:h2:file:./target/DATA_TYPES",
        "rootUser", "rootPass");
    if (jdbcClient is jdbc:Client) {
        sql:Error? err = initializeTable(jdbcClient);
        if (err is sql:Error) {
            io:println("Sample data table initialization failed: ", err);
        } else {
            // Execute the complex data type queries.
            queryBinaryType(jdbcClient);
            queryArrayType(jdbcClient);
            queryDateTimeType(jdbcClient);
            io:println("Sample executed successfully!");
        }
        // Close the JDBC client.
        sql:Error? e = jdbcClient.close();
    } else {
        io:println("Initialization failed: ", jdbcClient);
    }
}
