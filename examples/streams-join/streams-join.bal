import ballerina/http;
import ballerina/mime;
import ballerina/io;


type ProductMaterial {
    string name;
    float amount;
};

type MaterialUsage {
    string name;
    float totalRawMaterial;
    float totalConsumption;
};

//Streams that use 'ProductMaterial' as the constraint type.
//These streams are the input the streaming execution that performed within forever.
stream<ProductMaterial> rawMaterialStream;
stream<ProductMaterial> productionInputStream;

//This is the output stream that contains the events/alerts that generated based on streaming logic.
stream<MaterialUsage> materialUsageStream;

//Initialize the function that contains streaming queries.
future ftr= async initRealtimeProductionAlert();


function initRealtimeProductionAlert() {

    //Whenever materialUsageStream receives an event from the streaming rules defined in the forever block,
    //'printMaterialUsageAlert' function will be invoked.
    materialUsageStream.subscribe(printMaterialUsageAlert);


    //Gather events related to raw materials through 'rawMaterialStream' stream and production related events
    //through 'productionInputStream'. Here we calculate the usage of raw materials and production outcome of last
    //10 seconds and trigger an alert if raw material usage is 5% higher than the production outcome.
    //This forever block will be executed once, when initializing the service. So each time requestStream or
    //productionInputStream receives an event, the processing will happen asynchronously.
    forever {
        from productionInputStream window time(10000) as p
        join rawMaterialStream window time(10000) as r
        on r.name == p.name
        select r.name, sum(r.amount) as totalRawMaterial, sum(p.amount) as totalConsumption
        group by r.name
        having ((totalRawMaterial - totalConsumption) * 100.0 / totalRawMaterial) > 5
        => (MaterialUsage[] materialUsages) {
            //'materialUsages' are the output of the streaming rules and those are published to materialUsageStream.
            //Select clause should match with the structure of the 'MaterialUsage' type.
            materialUsageStream.publish(materialUsages);
        }
    }
}

function printMaterialUsageAlert(MaterialUsage materialUsage) {

    float materialUsageDifference = (materialUsage.totalRawMaterial - materialUsage.totalConsumption) * 100.0 /
        (materialUsage.totalRawMaterial);

    io:println("ALERT!! : Material usage is higher than the expected limit for material: " +
            materialUsage.name + " , usage difference (%): " + materialUsageDifference);
}

endpoint http:Listener productMaterialListener {
    port:9090
};

//Service which receives events related to production outcome and raw material input.
@http:ServiceConfig {
    basePath:"/"
}
service productMaterialService bind productMaterialListener {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/rawmaterial"
    }
    rawmaterialrequests(endpoint outboundEP, http:Request req) {
        var jsonMsg = req.getJsonPayload();
        match jsonMsg {
            json msg => {
                var productMaterial = check <ProductMaterial>msg;
                rawMaterialStream.publish(productMaterial);

                http:Response res = new;
                res.setJsonPayload("{'message' : 'Raw material request successfully received'}");
                _ = outboundEP -> respond(res);

            }
            http:PayloadError err => {
                http:Response res = new;
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = outboundEP -> respond(res);
            }
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/productionmaterial"
    }
    productionmaterialrequests(endpoint outboundEP, http:Request req) {
        var jsonMsg = req.getJsonPayload();
        match jsonMsg {
            json msg => {
                var productMaterial = check <ProductMaterial>msg;
                productionInputStream.publish(productMaterial);

                http:Response res = new;
                res.setJsonPayload("{'message' : 'Production input request successfully received'}");
                _ = outboundEP -> respond(res);

            }
            http:PayloadError err => {
                http:Response res = new;
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = outboundEP -> respond(res);
            }
        }

    }
}
