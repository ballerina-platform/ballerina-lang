import ballerina/lang.'transaction as transactions;
import ballerina/io;
import ballerina/java.jdbc;

public function main() returns error? {
    // The JDBC Client for the H2 database.
    jdbc:Client dbClient =
                check new (url = "jdbc:h2:file:./local-transactions/accountdb",
                           user = "test", password = "test");

    // Create the database table and populate some records.
    _ = check dbClient->execute("CREATE TABLE IF NOT EXISTS ACCOUNT " +
                                "(ID INTEGER, BALANCE DECIMAL, " +
                                "PRIMARY KEY(id))");
    _ = check dbClient->execute("INSERT INTO ACCOUNT VALUES (1, 2500.0)");
    _ = check dbClient->execute("INSERT INTO ACCOUNT VALUES (2, 1000.0)");

    // This is a `transaction` statement block. It is a must to have either
    // a commit action or a rollback statement in it. The transaction scope ends after
    // the commit action or rollback statement.
    transaction {
        // Execute database operations within the transaction
        var creditResult = dbClient->execute(
                                       "UPDATE ACCOUNT " +
                                       "SET BALANCE=BALANCE+500.0 WHERE ID=1");
        var debitResult = dbClient->execute(
                                       "UPDATE ACCOUNT " +
                                       "SET BALANCE=BALANCE-500.0 WHERE ID=2");

        // Returns information about the current transactions.
        transactions:Info transInfo = transactions:info();
        io:println("Transaction Info: ", transInfo);

        // The `commit` action performs the commit operation of the current transaction.
        // The result of the commit action will be either `error` or `()`.
        var commitResult = commit;
        if commitResult is () {
            // Operations to be executed after the transaction are committed successfully.
            io:println("Transaction committed");
            io:println("Account Credit: ", creditResult);
            io:println("Account Debit: ", debitResult);
        } else {
            // Operations to be executed if the transaction commit failed.
            io:println("Transaction failed");
        }
    }

    _ = check dbClient->execute("DROP TABLE ACCOUNT");
    
    // Close the JDBC client.
    check dbClient.close();
}
