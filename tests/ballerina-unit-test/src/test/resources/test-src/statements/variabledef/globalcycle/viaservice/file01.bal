import ballerina/http;

listener http:Listener lis = new http:Listener(port);
string b7a = "b7a";

int port = o.p;

Obj o = new();

service hello on lis {
    string str;
    int p = port;

    function __init() {
        self.str = b7a;
    }

    resource function sayHello(http:Caller caller, http:Request request) {

        http:Response response = new;
        response.setTextPayload(self.str);
        _ = caller -> respond(response);
    }
}

type Obj object {
    string str;
    int p = port;

    function __init() {
        self.str = b7a;
    }
};
