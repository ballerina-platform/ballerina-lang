import ballerina/http;
import ballerina/log;
import ballerina/websub;

endpoint http:Listener publisherServiceEP {
    port:9291
};

service<http:Service> original bind publisherServiceEP {
    one(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["http://localhost:9291/redirected/one"]);
    }

    two(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_FOUND_302, ["http://localhost:9291/redirected/two"]);
    }
}

service<http:Service> redirected bind publisherServiceEP {
    one(endpoint caller, http:Request req) {
        http:Response res = new;
        res = websub:addWebSubLinkHeader(res, ["http://localhost:9291/hub/one"], "http://redirectiontopicone.com");
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }

    two(endpoint caller, http:Request req) {
        http:Response res = new;
        res = websub:addWebSubLinkHeader(res, ["http://localhost:9291/hub/two"], "http://redirectiontopictwo.com");
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}

service<http:Service> hub bind publisherServiceEP {
    one(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["https://localhost:9595/websub/hub"]);
    }

    two(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_PERMANENT_REDIRECT_308, ["https://localhost:9595/websub/hub"]);
    }
}
