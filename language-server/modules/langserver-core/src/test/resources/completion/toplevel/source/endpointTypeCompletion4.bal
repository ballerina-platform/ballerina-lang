import ballerina/http;
import ballerina/swagger;

endpoint http:

service<http:Service> cbrService {
    cbrResource (endpoint caller, http:Request request) {
        
    }
}