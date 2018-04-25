import ballerina/http;
import ballerina/sql;
import ballerina/jdbc;

endpoint http:Listener testEp {
    port:9090
};

endpoint jdbc:Client testDB {
    url: "jdbc:hsqldb:hsql://localhost:9001/TEST_DB",
    username: "SA",
    poolOptions: {maximumPoolSize:10}
};

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> metricsTest bind testEp {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    test (endpoint caller, http:Request req) {
        http:Response resp = new;
        resp.setStringPayload("Metric Test!");
        _ = caller -> respond(resp);
    }

    @http:ResourceConfig {
        path: "/product"
    }
    getProduct (endpoint caller, http:Request req) {
        var dbResult = testDB -> select("SELECT * FROM Products", null);
        table dbTable = check dbResult;
        json jData = check <json>dbTable;
        string result;
        result = jData.toString();
        http:Response resp = new;
        resp.setStringPayload(result);
        _ = caller -> respond(resp);
    }
}
