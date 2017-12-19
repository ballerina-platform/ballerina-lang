import lang.annotations.doc;
import ballerina.net.http;

@doc:Description{value:1234}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(http:Connection con, http:Request req) {
        http:Response res = {};
        _ = con.respond(res);
    }
    
    @doc:Description{value:"Check order status"}
    resource checkStatus(http:Connection con, http:Request req) {
        http:Response res = {};
        _ = con.respond(res);
    }
}
