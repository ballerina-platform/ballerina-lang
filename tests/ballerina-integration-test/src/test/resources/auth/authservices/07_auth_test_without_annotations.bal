import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:APIListener listener7 {
    port:9096
};

service<http:Service> echo7 bind listener7 {
    test7 (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
