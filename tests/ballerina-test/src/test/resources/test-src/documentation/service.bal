import ballerina/http;

endpoint http:Listener echoEP { // TODO: Need to replace with http mock endpoint once it's fixed
    port:9090
};

documentation {PizzaService HTTP Service}
service<http:Service> PizzaService bind echoEP {

    documentation {Check orderPizza resource. P{{conn}} HTTP connection. P{{req}} In request.}
    orderPizza(endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> respond(res);
    }

    documentation {Check status resource. P{{conn}} HTTP connection. P{{req}} In request.}
    checkStatus(endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> respond(res);
    }
}
