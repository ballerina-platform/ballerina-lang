import ballerina/http;
import ballerina/jsonutils;
import ballerina/log;
import ballerinax/java.jdbc;

// JDBC Client for MySQL database. This client can be used with any JDBC
// supported database by providing the corresponding JDBC URL.
jdbc:Client testDB = new ({
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "test",
    password: "test",
    dbOptions: {useSSL: false}
});

// Create an HTTP service and bind it to a listener on port 9090.
service dataService on new http:Listener(9090) {

    resource function getData(http:Caller caller, http:Request req) {
        http:Response res = new;

        // Invoke `select` remote function on the MySQL database client
        // and obtain a large amount of data.
        var selectRet = testDB->select("SELECT * FROM Data", ());
        if (selectRet is table<record {}>) {
            // Convert the obtained data to `json`. Note that this conversion
            // does not load all the data into memory.
            // The `table` can be converted to `xml` in a similar manner.
            json jsonConversionRet = jsonutils:fromTable(selectRet);
            // Set the `json` payload to the response. This is streamed
            // to the client once the service is invoked.
            res.setPayload(<@untainted>jsonConversionRet);
        } else {
            // Set a payload indicating an error in case the data retrieval
            // fails.
            res.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            res.setPayload({"Error": "Error occured while retrieving data " +
                "from the database"
            });
        }

        // Respond to the client.
        var respondRet = caller->respond(res);
        if (respondRet is error) {
            log:printError("Sending response failed", respondRet);
        }
    }
}
