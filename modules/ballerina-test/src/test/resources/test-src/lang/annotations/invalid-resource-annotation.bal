import lang.annotations.doc;
import ballerina.net.http;

@doc:Description{value:"Pizza service"}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(http:Request req, http:Response res) {
        _ = res.send();
    }
    
    @doc:Description{value:456}
    resource checkStatus(http:Request req, http:Response res) {
        _ = res.send();
    }
}
