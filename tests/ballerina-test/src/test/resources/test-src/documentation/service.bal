import ballerina.net.http;

documentation { PizzaService HTTP Service }
service<http> PizzaService {

    documentation {
    Check orderPizza resource.
    - #conn HTTP connection.
    - #req In request.
    }
    resource orderPizza(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }

    documentation {
    Check status resource.
    - #conn HTTP connection.
    - #req In request.
    }
    resource checkStatus(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }
}
