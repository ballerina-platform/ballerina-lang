import ballerina/http;
import ballerina/runtime;

type ProductMaterial record {
    string name;
    float amount;
};

type MaterialUsage record {
    string name;
    float totalRawMaterial;
    float totalConsumption;
};

// These are the input streams that use `ProductMaterial` as the constraint type.
stream<ProductMaterial> rawMaterialStream = new;
stream<ProductMaterial> productionInputStream = new;

// This is the output stream that contains the events/alerts that are generated based on streaming logic.
stream<MaterialUsage> materialUsageStream = new;

string[] alerts = [];

function getAlerts() returns string {
    int count = 0;
    while (alerts.length() == 0 && count < 10) {
        runtime:sleep(1500);
        count = count + 1;
    }
    string[] copy = alerts.clone();
    alerts = [];
    return copy[0];
}

function initRealtimeProductionAlert() returns () {

    // Whenever the `materialUsageStream` stream receives an event from the streaming rules defined in the `forever`
    // block, the `printMaterialUsageAlert` function is invoked.
    materialUsageStream.subscribe(function (MaterialUsage e) {printMaterialUsageAlert(e);});


    // Gather events related to raw materials through the `rawMaterialStream` stream and production related events
    // through the `productionInputStream`. The raw materials usage and production outcome for the last
    // 10 seconds are calculated and an alert is triggered if the raw material usage is 5% higher than the
    // production outcome. This `forever` block is executed once, when initializing the service. The processing happens
    // asynchronously each time the `requestStream` or `productionInputStream` receives an event.
    forever {
        from productionInputStream window time(10000) as p
        join rawMaterialStream window time(10000) as r
        on r.name == p.name
        select r.name, sum(r.amount) as totalRawMaterial,
        sum(p.amount) as totalConsumption
        group by r.name
        having ((totalRawMaterial - totalConsumption) * 100.0 / totalRawMaterial) > 5
        => (MaterialUsage[] materialUsages) {
        // `materialUsages` is the output that matches the defined streaming rules. It is published to the `materialUsageStream` stream.
        // The selected clause should match the structure of the `MaterialUsage` type.
            foreach var usage in materialUsages {
                materialUsageStream.publish(usage);
            }
        }
    }
}

function printMaterialUsageAlert(MaterialUsage materialUsage) {
    float materialUsageDifference =
        (materialUsage.totalRawMaterial - materialUsage.totalConsumption) * 100.0 / (materialUsage.totalRawMaterial);

    alerts[alerts.length()] =
        "ALERT!! : Material usage is higher than the expected limit for material : "
        + materialUsage.name + ", usage difference (%) : " + materialUsageDifference;
}

listener http:Listener productMaterialListener = new (9090);

// The service, which receives events related to the production outcome and the raw material input.
@http:ServiceConfig {
    basePath: "/"
}
service productMaterialService on productMaterialListener {

    // Initialize the function that contains streaming queries.
    future<()> ftr = start initRealtimeProductionAlert();

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/rawmaterial"
    }
    resource function rawmaterialrequests(http:Caller caller, http:Request req) {
        var jsonMsg = req.getJsonPayload();
        if (jsonMsg is json) {
            var productMaterial = ProductMaterial.convert(jsonMsg);
            rawMaterialStream.publish(productMaterial);

            http:Response res = new;
            res.setJsonPayload({"message": "Raw material request"
                    + " successfully received"});
            _ = caller->respond(res);
        } else if (jsonMsg is error) {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(untaint jsonMsg.reason());
            _ = caller->respond(res);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/productionmaterial"
    }
    resource function productionmaterialrequests(http:Caller caller,
                                                 http:Request req) {
        var jsonMsg = req.getJsonPayload();
        if (jsonMsg is json) {
            var productMaterial = ProductMaterial.convert(jsonMsg);
            productionInputStream.publish(productMaterial);

            http:Response res = new;
            res.setJsonPayload({"message": "Production input " +
                    "request successfully received"});
            _ = caller->respond(res);

        } else if (jsonMsg is error) {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(untaint jsonMsg.reason());
            _ = caller->respond(res);
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/alerts"
    }
    resource function alerts(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload(getAlerts());
        _ = caller->respond(res);
    }
}
