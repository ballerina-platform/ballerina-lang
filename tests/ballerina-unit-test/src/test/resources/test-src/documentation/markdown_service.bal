import ballerina/http;

listener http:MockListener echoEP = new(9090);

# PizzaService HTTP Service
service PizzaService on echoEP {

    # Check orderPizza resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function orderPizza(http:Caller conn, http:Request req) {
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    # Check status resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function checkStatus(http:Caller conn, http:Request req) {
        http:Response res = new;
        checkpanic conn->respond(res);
    }
}
