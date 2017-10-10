import lang.annotations.doc;
import ballerina.net.http;
import ballerina.net.http.response;

@doc:Description{value:"Pizza service"}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(http:Request req, http:Response res) {
        response:send(res);
    }
    
    @doc:Description{value:456}
    resource checkStatus(http:Request req, http:Response res) {
        response:send(res);
    }
}
