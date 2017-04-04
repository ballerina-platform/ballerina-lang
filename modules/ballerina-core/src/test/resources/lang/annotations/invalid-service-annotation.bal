import lang.annotations.doc;

@doc:Description{value:1234}
service PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(message m) {
        reply m;
    }
    
    @doc:Description{value:"Check order status"}
    resource checkStatus(message m) {
        reply m;
    }
}
