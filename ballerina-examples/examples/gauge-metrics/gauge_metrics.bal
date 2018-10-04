import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/observe;

// Make sure you start the service with `--observe`, or metrics enabled.
@http:ServiceConfig { basePath: "/online-store-service" }
service<http:Service> onlineStoreService bind { port: 9090 } {

    //Create a gauge as a global varaible in the service with optional field description,
    //and default statistics configurations = { timeWindow: 600000, buckets: 5,
    // percentiles: [0.33, 0.5, 0.66, 0.99] }.
    observe:Gauge globalGauge = new("global_gauge", desc = "Global gauge defined");

    @http:ResourceConfig {
        path: "/make-order"
    }
    makeOrder(endpoint caller, http:Request req) {
        io:println("------------------------------------------");
        //Incrementing the global gauge defined by 15.0.
        globalGauge.increment(amount = 15.0);
        //Log the current state of global gauge.
        printGauge(globalGauge);


        //Create a gauge with simply a name, and default statistics configurations.
        observe:Gauge localGauge = new("local_operations");
        //Increment the local gauge by default value 1.0.
        localGauge.increment();
        //Increment the value of the gauge by 20.
        localGauge.increment(amount = 20.0);
        //Decrement the local gauge by default value 1.0.
        localGauge.decrement();
        //Decrement the value of the gauge by 20.
        localGauge.decrement(amount = 10.0);
        //Log the current state of local gauge.
        printGauge(localGauge);


        //Create a gauge with optional fields description, and tags defined.
        observe:Gauge registeredGaugeWithTags = new("registered_gauge_with_tags",
            desc = "RegisteredGauge", tags = { property: "gaugeProperty", gaugeType: "RegisterType" });

        //Register the gauge instance, therefore it is stored in the global registry and can be reported to the
        //metrics server such as Prometheus. Additionally, this operation will register to the global registry for the
        //first invocation and will throw an error if there is already a registration of different metrics instance
        //or type. And subsequent invocations of register() will simply retrieve the stored metrics instance
        //for the provided name and tags fields, and use that instance for the subsequent operations on the
        //counter instance.
        _ = registeredGaugeWithTags.register();
        //Set the value of the gauge with the new value.
        registeredGaugeWithTags.increment();
        float value = registeredGaugeWithTags.getValue();
        float newValue = value * 12;
        registeredGaugeWithTags.setValue(newValue);
        //Log the current state of registered gauge with tags.
        printGauge(registeredGaugeWithTags);


        //Create a gauge with statistics disabled by passing empty statistics config array.
        observe:StatisticConfig[] statsConfigs = [];
        observe:Gauge gaugeWithNoStats = new("gauge_with_no_stats", desc = "Some description",
            statisticConfig = statsConfigs);
        gaugeWithNoStats.setValue(100);
        printGauge(gaugeWithNoStats);


        //Create gauge with custom statistics config.
        observe:StatisticConfig config = { timeWindow: 30000, percentiles: [0.33, 0.5, 0.9, 0.99], buckets: 3 };
        statsConfigs[0] = config;
        observe:Gauge gaugeWithCustomStats = new("gauge_with_custom_stats", desc = "Some description"
            , statisticConfig = statsConfigs);
        int i = 1;
        while (i < 6) {
            gaugeWithCustomStats.setValue(100 * i);
            i = i + 1;
        }
        //Log the current state of registered gauge with tags.
        printGauge(gaugeWithCustomStats);

        io:println("------------------------------------------");

        //Send reponse to the client.
        http:Response res = new;
        // Use a util method to set a string payload.
        res.setPayload("Order Processed!");

        // Send the response back to the caller.
        caller->respond(res) but {
            error e => log:printError(
                           "Error sending response", err = e)
        };
    }
}

function printGauge(observe:Gauge gauge) {
    //Get the statistics snapshot of the gauge.
    io:print("Gauge - " + gauge.name + " Snapshot: ");
    io:println(gauge.getSnapshot());
    //Get the current value of the gauge.
    io:println("Gauge - " + gauge.name + " Current Value: " + gauge.getValue());
}
