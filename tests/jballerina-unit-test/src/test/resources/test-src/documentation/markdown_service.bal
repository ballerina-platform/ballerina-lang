import ballerina/http;

listener http:MockListener echoEP = new(9090);
listener http:MockListener echoEP2 = new(9091);

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

# Test type `typeDef`
# Test service `helloWorld`
# Test variable `testVar`
# Test var `testVar`
# Test function `add`
# Test parameter `x`
# Test const `constant`
# Test annotation `annot`
service PizzaService2 on echoEP2 {

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
