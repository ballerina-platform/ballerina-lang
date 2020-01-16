import ballerina/io;
import ballerinax/java.jdbc;

public function main() {
    jdbc:Client testDB = new ({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "test",
        password: "test",
        dbOptions: {useSSL: false}
    });

    // Create a table for data insertion.
    var ret = testDB->update("CREATE TABLE Data (id INT, field1 " +
    "VARCHAR(1024), field2 VARCHAR(1024));");
    handleUpdate(ret, "Create Data table");

    // Create the stored procedure with row_count IN parameter.
    ret = testDB->update("CREATE PROCEDURE PopulateData(IN row_count INT) " +
       "BEGIN " +
           "DECLARE count INT; " +
           "DECLARE strDataEntry VARCHAR(1024); " +
           "SET count = 1; " +
           "SET strDataEntry =  ''; " +
           "WHILE count <= 1024 DO " +
                       "SET strDataEntry = CONCAT(strDataEntry, 'x'); " +
                       "SET count = count + 1; " +
           "END WHILE; " +
           "SET count = 1; " +
           "WHILE count <= row_count DO " +
                       "INSERT INTO Data VALUES (count, strDataEntry, strDataEntry); " +
                       "SET count = count + 1; " +
           "END WHILE; " +
           "SELECT strDataEntry; " +
       "END");
    handleUpdate(ret, "Stored procedure with IN param creation");

    // Call stored procedure. This inserts around 200MB of textual data.
    // You can increment the row_count to increase the amount of data.
    var retCall = testDB->call("CALL PopulateData(?)", (), 100000);
    if (retCall is error) {
        io:println("Stored procedure call failed: ",
                    <string>retCall.detail()?.message);
    } else {
        io:println("Call operation is successful");
    }
}

// Function to handle the return value of the update remote function.
function handleUpdate(jdbc:UpdateResult|jdbc:Error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message, " status: ", returned.updatedRowCount);
    } else {
        io:println(message, " failed: ", <string>returned.detail()?.message);
    }
}
