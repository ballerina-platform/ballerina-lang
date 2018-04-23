import ballerina/http;
import ballerina/swagger;

endpoint http: listener {
    
};

@http:ServiceConfig {
    basePath: "/cbr"
}
service<http:Service> cbrService {
    cbrResource (endpoint caller, http:Request request) {
        
    }
}