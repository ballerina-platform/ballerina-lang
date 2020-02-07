import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/observe;

//Create a gauge as a global variable in the service with optional field description,
//and default statistics configurations = { timeWindow: 600000, buckets: 5,
// percentiles: [0.33, 0.5, 0.66, 0.99] }.
observe:Gauge globalGauge = new ("global_gauge", "Global gauge defined");

// Make sure you start the service with the '--b7a.observability.enabled=true' property or with metrics enabled.
@http:ServiceConfig {
    basePath: "/online-store-service"
}
service onlineStoreService on new http:Listener(9090) {

    @http:ResourceConfig {
        path: "/make-order"
    }
    resource function makeOrder(http:Caller caller, http:Request req) {
        io:println("------------------------------------------");
        //Incrementing the global gauge defined by 15.0.
        globalGauge.increment(15.0);
        //Log the current state of global gauge.
        printGauge(globalGauge);


        //Create a gauge with simply a name, and default statistics configurations.
        observe:Gauge localGauge = new ("local_operations");
        //Increment the local gauge by default value 1.0.
        localGauge.increment();
        //Increment the value of the gauge by 20.
        localGauge.increment(20.0);
        //Decrement the local gauge by default value 1.0.
        localGauge.decrement();
        //Decrement the value of the gauge by 20.
        localGauge.decrement(10.0);
        //Log the current state of local gauge.
        printGauge(localGauge);


        //Create a gauge with optional fields description, and tags defined.
        observe:Gauge registeredGaugeWithTags = new ("registered_gauge_with_tags",
            "RegisteredGauge",
            {property: "gaugeProperty", gaugeType: "RegisterType"});

        //Register the gauge instance, therefore it is stored in the global registry and can be reported to the
        //metrics server such as Prometheus. Additionally, this operation will register to the global registry for the
        //first invocation and will throw an error if there is already a registration of different metrics instance
        //or type. And subsequent invocations of register() will simply retrieve the stored metrics instance
        //for the provided name and tags fields, and use that instance for the subsequent operations on the
        //counter instance.
        error? result = registeredGaugeWithTags.register();
        if (result is error) {
            log:printError("Error in registering gauge", err = result);
        }

        //Set the value of the gauge with the new value.
        registeredGaugeWithTags.increment();
        float value = registeredGaugeWithTags.getValue();
        float newValue = value * 12;
        registeredGaugeWithTags.setValue(newValue);
        //Log the current state of registered gauge with tags.
        printGauge(registeredGaugeWithTags);


        //Create a gauge with statistics disabled by passing empty statistics config array.
        observe:StatisticConfig[] statsConfigs = [];
        observe:Gauge gaugeWithNoStats = new ("gauge_with_no_stats",
                                        "Some description", (), statsConfigs);
        gaugeWithNoStats.setValue(100);
        printGauge(gaugeWithNoStats);


        //Create gauge with custom statistics config.
        observe:StatisticConfig config = {
            timeWindow: 30000,
            percentiles: [0.33, 0.5, 0.9, 0.99],
            buckets: 3
        };
        statsConfigs[0] = config;
        observe:Gauge gaugeWithCustomStats = new ("gauge_with_custom_stats",
                                        "Some description", (), statsConfigs);
        int i = 1;
        while (i < 6) {
            gaugeWithCustomStats.setValue(100.0 * i);
            i = i + 1;
        }
        //Log the current state of registered gauge with tags.
        printGauge(gaugeWithCustomStats);

        io:println("------------------------------------------");

        //Send response to the client.
        http:Response res = new;
        // Use a util method to set a string payload.
        res.setPayload("Order Processed!");

        // Send the response back to the caller.
        result = caller->respond(res);

        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}

function printGauge(observe:Gauge gauge) {
    //Get the statistics snapshot of the gauge.
    io:print("Gauge - " + gauge.name + " Snapshot: ");
    observe:Snapshot[]? snapshots = gauge.getSnapshot();
    json|error snapshotAsAJson = json.constructFrom(snapshots);
    if snapshotAsAJson is json {
        io:println(snapshotAsAJson.toJsonString());
    }
    //Get the current value of the gauge.
    io:println("Gauge - ", gauge.name, " Current Value: "
        , gauge.getValue());
}
