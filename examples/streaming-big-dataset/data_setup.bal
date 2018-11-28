import ballerina/io;
import ballerina/mysql;

public function main() {
    mysql:Client testDB = new({
            host: "localhost",
            port: 3306,
            name: "StreamTestDB",
            username: "user1",
            password: "pass1",
            poolOptions: { maximumPoolSize: 5 }
        });

    // Create table for data insertion.
    var ret = testDB->update("CREATE TABLE Data (id INT, field1
        VARCHAR(1024), field2 VARCHAR(1024));");
    handleUpdate(ret, "Create Data table");

    // Create the stored procedure with row_count IN parameter.
    ret = testDB->update("CREATE PROCEDURE PopulateData(IN row_count INT)
       BEGIN
           DECLARE count INT;
           DECLARE strDataEntry VARCHAR(1024);
           SET count = 1;
           SET strDataEntry =  '';
           WHILE count <= 1024 DO
                       SET strDataEntry = CONCAT(strDataEntry, 'x');
                       SET count = count + 1;
           END WHILE;
           SET count = 1;
           WHILE count <= row_count DO
                       INSERT INTO Data VALUES (count, strDataEntry, strDataEntry);
                       SET count = count + 1;
           END WHILE;
           SELECT strDataEntry;
       END");
    handleUpdate(ret, "Stored procedure with IN param creation");

    // Call stored procedure. This inserts around 200MB of textual data.
    // You can increment the row_count to increase the amount of data.
    var retCall = testDB->call("CALL PopulateData(?)", (), 100000);
    if (retCall is ()|table<record {}>[]) {
        io:println("Call operation is successful");
    } else if (retCall is error) {
        io:println("Stored procedure call failed: " + <string>retCall.detail().message);
    }
}

// Function to handle return values of the update operation.
function handleUpdate(int|error returned, string message) {
    if (returned is int) {
        io:println(message + " status: " + returned);
    } else if (returned is error) {
        io:println(message + " failed: " + <string>returned.detail().message);
    }
}
