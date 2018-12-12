import ballerina/http;

@http:ServiceConfig {
    basePath:"/"
}
service resourceReturnService on new http:Listener(9228) {

    resource function manualErrorReturn(http:Caller caller, http:Request request) returns error? {
        http:Response response = new;
        response.setTextPayload("Hello Ballerina!");

        // Manually return error.
        if (1 == 1) {
            error e = error("Simulated error");
            return e;
        }
        _ = caller -> respond(response);
        return;
    }

    resource function checkErrorReturn(http:Caller caller, http:Request request) returns error? {
        http:Response response = new;

        // Check expression returns error.
        int i = check getError();
        response.setTextPayload("i = " + i);
        _ = caller -> respond(response);
        return;
    }

}

function getError() returns error|int {
    if (1 == 1) {
        error e = error("Simulated error");
        return e;
    }
    return 1;
}
