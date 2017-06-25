import ballerina.lang.system;
import ballerina.data.sql;
import ballerina.lang.errors;

function main (string[] args) {
    //Create a SQL connector.
    map props = {"jdbcUrl":"jdbc:mysql://localhost:3306/db",
                    "username":"root", "password":"root"};
    sql:ClientConnector testDB = create
                        sql:ClientConnector(props);
    //Try catch is used here since update action can throw
    //errors due to SQL errors, connection pool errors etc.
    try {
        transaction {
            sql:Parameter[] parameters = [];
            //This is the first action participate in the transaction.
            int count = sql:ClientConnector.update(testDB,
              "Insert into Customers(id,name) values
              (1, 'Anne')", parameters);

            //This is the second action participate in the transaction.
            count = sql:ClientConnector.update(testDB,
               "Insert into Salary (id, salary) values
               (1, 2500)", parameters);
            system:println("Inserted count:" + count);

            //Anytime the transaction can be forcefully aborted
            //using the abort keyword.
            if (count == 0) {
                abort;
            }
            //The end curly bracket marks the end of the transaction
            //and the transaction will be committed or rollbacked at
            //this point.
        } aborted {
            //The aborted section get executed if the transaction is
            //rollbacked due to exception, abort statement or throw statement
            system:println("Transaction aborted");
        } committed {
            //The committed section get exectuted if the transaction
            //is successfully committed.
            system:println("Transaction committed");
        }
    } catch (errors:Error err) {
        //Catching the errors if there is any.
        system:println("Error occured");
    }
    //Close the connection pool.
    sql:ClientConnector.close(testDB);
}
