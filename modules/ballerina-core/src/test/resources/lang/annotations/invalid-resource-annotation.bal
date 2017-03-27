import lang.annotations.doc;

@doc:Description{value:"Pizza service"}
service PizzaService {
    
    @doc:Description{value:"Order pizza"}
    resource orderPizza(message m) {
        reply m;
    }
    
    @doc:Description{value:456}
    resource checkStatus(message m) {
        reply m;
    }
}
