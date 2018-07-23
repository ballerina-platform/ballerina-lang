import ballerina/http;

endpoint http:Listener echoEP { // TODO: Need to replace with http mock endpoint once it's fixed
    port:9090
};

# PizzaService HTTP Service
service<http:Service> PizzaService bind echoEP {

    # Check orderPizza resource.
    # + conn - HTTP connection.
    # + req - In request.
    orderPizza(endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> respond(res);
    }

    # Check status resource.
    # + conn - HTTP connection.
    # + req - In request.
    checkStatus(endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> respond(res);
    }
}
