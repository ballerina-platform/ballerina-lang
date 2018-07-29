import ballerina/http;
import ballerina/io;
import ballerina/h2;

endpoint h2:Client testDB {
    path: "../../tempdb/",
    name: "STREAMING_XML_TEST_DB",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 1 },
    dbOptions: { IFEXISTS: true }
};

service<http:Service> dataService bind { port: 9090 } {

    getData(endpoint caller, http:Request req) {
        http:Response res = new;

        var selectRet = testDB->select("SELECT * FROM Data", ());
        table dt;
        match selectRet {
            table tableReturned => dt = tableReturned;
            error e => io:println("Select data from Data table failed: " + e.message);
        }

        xml xmlConversionRet = check <xml>dt;
        res.setPayload(untaint xmlConversionRet);

        caller->respond(res) but {
            error e => io:println("Error sending response")
        };
    }
}
