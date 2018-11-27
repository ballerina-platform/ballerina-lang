import ballerina/http;
import ballerina/log;
import ballerina/mysql;

// Create MySQL client endpoint.
mysql:Client testDB = new({
        host: "localhost",
        port: 3306,
        name: "StreamTestDB",
        username: "user1",
        password: "pass1",
        poolOptions: { maximumPoolSize: 5 }
    });

// Create a HTTP service and bind to a listener on port 9090
service dataService on new http:Listener(9090) {

    resource function getData(http:Caller caller, http:Request req) {
        http:Response res = new;

        // Invoke `select` action on the MySQL database endpoint and obtain a large amount of data.
        var selectRet = testDB->select("SELECT * FROM Data", ());
        if (selectRet is table <record {}>) {
            // Convert the obtained data to JSON. Note that this conversion does not load
            // all the data into memory.
            // The table can be converted to XML in a similar manner.
            var jsonConversionRet = json.create(selectRet);
            if (jsonConversionRet is json) {
                // Set the JSON payload to the response. This is streamed to the client once the service is
                // invoked.
                res.setPayload(untaint jsonConversionRet);
            } else {
                // Set a payload indicating an error in case the JSON conversion fails.
                res.setPayload({ "Error": "Error occured while converting the data to JSON" });
                res.statusCode = http:INTERNAL_SERVER_ERROR_500;
            }
        } else {
            // Set a payload indicating an error in case the data retrieval fails.
            res.statusCode = http:INTERNAL_SERVER_ERROR_500;
            res.setPayload({ "Error": "Error occured while retrieving data from the database" });
        }

        // Respond to the client.
        var respondRet = caller->respond(res);
        if (respondRet is error) {
            log:printError("Sending response failed", err = respondRet);
        }
    }
}
