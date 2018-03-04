import ballerina.io;
import ballerina.config;
import ballerina.net.http;

service<http> serviceName{
    resource resourceName (http:Connection conn, http:InRequest req) {
        string a = config:("property");
    }
}