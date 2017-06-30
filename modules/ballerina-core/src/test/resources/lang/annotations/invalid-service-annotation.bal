import lang.annotations.doc;
import ballerina.net.http;

@doc:Description{value:1234}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(message m) {
        reply m;
    }
    
    @doc:Description{value:"Check order status"}
    resource checkStatus(message m) {
        reply m;
    }
}
