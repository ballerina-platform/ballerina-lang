import lang.annotations.doc;
import ballerina.net.http;

@doc:Description{value:1234}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(http:Request req, http:Response res) {
        res.send();
    }
    
    @doc:Description{value:"Check order status"}
    resource checkStatus(http:Request req, http:Response res) {
        res.send();
    }
}
