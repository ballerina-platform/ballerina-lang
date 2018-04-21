
import ballerina/lang.messages;
import ballerina/http;
import ballerina/lang.system;
import ballerina/data.sql;

@http:configuration {
basePath: "/ABCBank"
}
service<http> ATMLocator {

    @http:resourceConfig {
    methods: [ "POST"]
}
    resource locator (message m) {
        sql:ConnectionProperties properties = {maximumPoolSize: 5};
        sql:ClientConnector empDB = create sql:ClientConnector (sql:MYSQL, "localhost", 3306, "db", "sa", "root", properties);
        http:ClientConnector bankInfoService = create http:ClientConnector ("http://localhost:9090/bankinfo/product");
        
        empDB.
        reply response;
    }
}
