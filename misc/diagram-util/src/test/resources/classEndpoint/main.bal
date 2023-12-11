import ballerina/http;

service /user on new http:Listener(9090) {

    http:Client httpEpS10;

    function init() returns error? {
        self.httpEpS10 = check new (url = "");
    }

    resource function post .() returns error? {

    }
}

