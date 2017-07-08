import lang.annotations.doc;
import ballerina.net.http;

@doc:Description{value:"Pizza service"}
service<http> PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(message m) {
        reply m;
    }
    
    @doc:Description{value:456}
    resource checkStatus(message m) {
        reply m;
    }
}
