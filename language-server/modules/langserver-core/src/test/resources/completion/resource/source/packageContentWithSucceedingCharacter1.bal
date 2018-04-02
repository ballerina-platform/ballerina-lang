import ballerina/io;
import ballerina/config;
import ballerina/http;

service<http> serviceName{
    resource resourceName (http:Connection conn, http:Request req) {
        string a = config:("property");
    }
}