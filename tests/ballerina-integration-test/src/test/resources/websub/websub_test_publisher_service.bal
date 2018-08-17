import ballerina/http;
import ballerina/log;
import ballerina/websub;

endpoint http:Listener publisherServiceEP {
    port:9290
};

service<http:Service> publisherService bind publisherServiceEP {
    discover(endpoint caller, http:Request req) {
        http:Response res = new;
        websub:addWebSubLinkHeader(res, ["https://localhost:9494/websub/hub"], "http://websubpubtopictwo.com");
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}
