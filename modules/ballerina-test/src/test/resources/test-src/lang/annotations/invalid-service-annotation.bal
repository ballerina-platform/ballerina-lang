import lang.annotations.doc;
import ballerina.net.http;
import ballerina.net.http.response;

@doc:Description{value:1234}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(http:Request req, http:Response res) {
        response:send(res);
    }
    
    @doc:Description{value:"Check order status"}
    resource checkStatus(http:Request req, http:Response res) {
        response:send(res);
    }
}
