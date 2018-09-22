import ballerina/http;
import ballerina/log;
import ballerina/websub;

endpoint http:Listener publisherServiceEPTwo {
    port:8081
};

service<http:Service> original bind publisherServiceEPTwo {
    one(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["http://localhost:8081/redirected/one"]);
    }

    two(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_FOUND_302, ["http://localhost:8081/redirected/two"]);
    }
}

service<http:Service> redirected bind publisherServiceEPTwo {
    one(endpoint caller, http:Request req) {
        http:Response res = new;
        websub:addWebSubLinkHeader(res, ["http://localhost:8081/hub/one"], WEBSUB_TOPIC_FIVE);
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }

    two(endpoint caller, http:Request req) {
        http:Response res = new;
        websub:addWebSubLinkHeader(res, ["http://localhost:8081/hub/two"], WEBSUB_TOPIC_SIX);
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}

service<http:Service> hub bind publisherServiceEPTwo {
    one(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["https://localhost:9191/websub/hub"]);
    }

    two(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_PERMANENT_REDIRECT_308, ["https://localhost:9191/websub/hub"]);
    }
}
