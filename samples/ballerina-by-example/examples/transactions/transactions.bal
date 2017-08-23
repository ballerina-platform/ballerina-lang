import ballerina.lang.system;
import ballerina.data.sql;

function main (string[] args) {
    sql:ConnectionProperties properties = {maximumPoolSize:5};
    sql:ClientConnector testDB = create sql:ClientConnector(
        sql:MYSQL, "localhost", 3306, "testdb", "test", "test", properties);
    //Create the tables required for the transaction.
    sql:Parameter[] emptyParams = [];
    testDB.update("CREATE TABLE IF NOT EXISTS CUSTOMER (ID INT, NAME VARCHAR(30))",
                  emptyParams);
    testDB.update("CREATE TABLE IF NOT EXISTS SALARY (ID INT, MON_SALARY FLOAT)",
                  emptyParams);
    //Here is the transaction block. You can use a Try catch here since update action can
    //throw errors due to SQL errors, connection pool errors etc.
    transaction {
        //This is the first action participate in the transaction.
        sql:Parameter[] parameters = [];
        int count = testDB.update("INSERT INTO CUSTOMER(ID,NAME)
                    VALUES (1, 'Anne')", parameters);
        //This is the second action participate in the transaction.
        count = testDB.update("INSERT INTO SALARY (ID, MON_SALARY)
                    VALUES (1, 2500)", parameters);

        system:println("Inserted count:" + count);
        //Anytime the transaction can be forcefully aborted
        //using the abort keyword.
        if (count == 0) {
            abort;
        }
        //The end curly bracket marks the end of the transaction
        //and the transaction will be committed or rolled back at
        //this point.
    } failed {
        //The failed block will be executed if the transaction is
        //failed due to an exception or a throw statement. This block
        //will execute each time transaction is failed until retry count
        //is reached.
        system:println("Transaction failed");
        //The retry count is the number times the transaction is
        //tried before aborting. By default a transaction is tried three
        //times before aborting. Only integer literals or constants are
        //allowed for retry count.
        retry 4;
    } aborted {
        //The aborted block will be executed if the transaction is
        //aborted using an abort statement or failed even after retrying
        //the specified count.
        system:println("Transaction aborted");
    } committed {
        //The committed block will be executed if the transaction
        //is successfully committed.
        system:println("Transaction committed");
    }
    //Close the connection pool.
    testDB.close();
}
