import ballerina/lang.'transaction as transactions;
import ballerina/io;
import ballerina/java.jdbc;

// The user-defined retry manager object.
public class MyRetryManager {
    private int count;
    public function init(int count = 2) {
        self.count = count;
    }
    public function shouldRetry(error e) returns boolean {
        if e is error && self.count >  0 {
            self.count -= 1;
            io:println("Retries remaining: ", self.count);
            return true;
        } else {
            return false;
        }
    }
}

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

    // In a retry scenario, if the the transaction block returns an error e.g., with a `check` expression, 
    // the transaction is automatically rollbacked and the retry manager is checked to retry the transaction.
    retry<MyRetryManager> (3) transaction {
        transactions:onRollback(onRollbackFunc);
        transactions:onCommit(onCommitFunc);

        // Execute database operations within the transaction.
        var creditResult = check dbClient->execute(
                                       "UPDATE ACCOUNT " +
                                       "SET BALANCE=BALANCE+500.0 WHERE ID=1");
        var debitResult = check dbClient->execute(
                                       "UPDATE ACCOUNT " +
                                       "SET BALANCE=BALANCE-500.0 WHERE ID=2");

        io:println("Transaction Info: ", transactions:info());

        check commit;
        io:println("Transaction committed.");
        io:println("Account Credit: ", creditResult);
        io:println("Account Debit: ", debitResult);
    }

    _ = check dbClient->execute("DROP TABLE ACCOUNT");
    
    // Close the JDBC client.
    check dbClient.close();
}

function onRollbackFunc(transactions:Info info,
                        error? cause, boolean willRetry) {
    io:println("Rollback handler executed.");
}

function onCommitFunc(transactions:Info info) {
    io:println("Commit handler executed.");
}
