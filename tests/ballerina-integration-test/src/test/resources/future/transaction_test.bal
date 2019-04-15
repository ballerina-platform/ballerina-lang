import ballerina/h2;
import ballerina/io;
import ballerina/runtime;

channel<string> strChn = new;
channel<int> intChn = new;
string trx = "start";

h2:Client testDB = new({
    path: "./target/tempdb/",
    name: "TEST_SQL_CONNECTOR_TR",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 }
});

 public function main() {
      // Start the future
      future<()> f1 = start startTrx();
      // Sleep for 1s
      runtime:sleep(1000);
      // Receive from channel
      string strResult = <- strChn, 66;
      // Cancel future
      boolean cancel_w1 = f1.cancel();
      // Send to channel
      100 -> intChn, 88;
      checkpanic testDB.stop();
 }

function startTrx() {
    // Send to channel
    "message" -> strChn, 66;
    transaction {
        trx = trx + " inTrx";
        io:println(trx);
        // Receive from channel
        int intResult = <- intChn, 88;
        _ = checkpanic testDB->update("Insert into FactoryEmployee (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 211, 5000.75, 'USA')");
        trx = trx + " endTrx";
        io:println(trx);
    } onretry {
        trx = trx + " retry";
        io:println(trx);
    } committed {
         trx = trx + " committed";
         io:println(trx);
    } aborted {
        trx = trx + " aborted";
        io:println(trx);
    }
}
