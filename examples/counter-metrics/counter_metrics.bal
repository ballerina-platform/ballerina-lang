import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/observe;

//Create a counter as a global variable in the service with optional field description.
observe:Counter globalCounter = new("total_orders",
                                    desc = "Total quantity required");

// Make sure you start the service with `--observe`, or metrics enabled.
@http:ServiceConfig {
    basePath: "/online-store-service"
}
service onlineStoreService on new http:Listener(9090) {

    @http:ResourceConfig {
        path: "/make-order"
    }
    resource function makeOrder(http:Caller caller, http:Request req) {
        //Incrementing the global counter defined with the default value 1.
        globalCounter.increment();

        //Create a counter with simply a name.
        observe:Counter localCounter = new("local_operations");
        localCounter.increment();
        //Increment the value of the counter by 20.
        localCounter.increment(amount = 20);

        //Create a counter with optional fields description, and tags.
        observe:Counter registeredCounter = new("total_product_order_quantity",
            desc = "Total quantity required",
            tags = {prodName:"HeadPhone", prodType:"Electronics"});

        //Register the counter instance, therefore it is stored in the global registry and can be reported to the
        //metrics server such as Prometheus. Additionally, this operation will register to the global registry for the
        //first invocation and will throw an error if there is already a registration of different metrics instance
        //or type. Subsequent invocations of register() will simply retrieve the stored metrics instance
        //for the provided name and tags fields, and use that instance for the subsequent operations on the
        //counter instance.
        _ = registeredCounter.register();

        //Increase the amount of the registered counter instance by amount 10.
        registeredCounter.increment(amount = 10);

        //Get the value of the counter instances.
        io:println("------------------------------------------");
        io:println("Global Counter = "+ globalCounter.getValue());
        io:println("Local Counter = "+ localCounter.getValue());
        io:println("Registered Counter = "+ registeredCounter.getValue());
        io:println("------------------------------------------");

        //Send reponse to the client.
        http:Response res = new;
        // Use a util method to set a string payload.
        res.setPayload("Order Processed!");

        // Send the response back to the caller.
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}
