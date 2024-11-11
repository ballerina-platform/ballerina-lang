import ballerina/http;

http:Client httpEpM0 = check new (url = "");

service /user on new http:Listener(9090) {

    http:Client httpEpS10;

    function init() returns error? {
        self.httpEpS10 = check new (url = "");
        self.httpEpS11 = check new (url = "");
        self.httpEpS12 = check new (url = "");
    }

    resource function get .() returns error? {
        http:Client httpEpL0 = check new (url = "");
    }

    http:Client httpEpS11;

    resource function post .() returns error? {
        @display {
            label: "function block level endpoint with display annotation"
        }
        http:Client httpEpL1 = check new (url = "");
    }

    http:Client httpEpS12;
}

public function main() {

}

@display {
    label: "module block level endpoint with display annotation"
}
http:Client httpEpM1 = check new (url = "");

service /products on new http:Listener(0) {
    @display {
        label: "class block level endpoint with display annotation"
    }
    http:Client httpEpS20;

    function init() returns error? {
        self.httpEpS20 = check new (url = "");
        self.httpEpS21 = check new (url = "");
    }

    resource function get .() {

    }

    http:Client httpEpS21;
}

http:Client _ = check new (url = "");

class User {

    http:Client httpEpC0 = check new (url = "");

    function init() returns error? {
        self.httpEpC1 = check new (url = "");
        self.httpEpC2 = check new (url = "");
    }

    http:Client httpEpC1;

    function getUser() {

    }

    http:Client httpEpC2; 
       
    http:Client httpEpC3 = check new (url = "");
}

http:Client httpEpM2 = check new (url = "");
