import ballerina/http;
import ballerina/mime;
endpoint http:Listener infoServiceEP {
    port:9092
};

@Description {value:"Consumes and Produces annotations contain MIME types as an array of strings."}
@http:ServiceConfig { endpoints:[infoServiceEP] ,basePath:"infoService"}
service<http:Service> infoService {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/",
        consumes:["text/json", "application/json"],
        produces:["application/xml"]
    }
    @Description {value:"Resource can consume/accept text/json and application/json media types only. Therefore Content-Type header must have one of the types."}
    @Description {value:"Resource can produce application/xml payloads. Therefore Accept header should be set accordingly."}
    student (endpoint conn, http:Request req) {
        //Get JSON payload from the request message.
        http:Response res = {};
        var msg = req.getJsonPayload();
        match msg {
            json jsonMsg => {
            //Get the string value relevant to the key "name".
                string nameString =? <string>jsonMsg["name"];
                //Create XML payload and respond back.
                string payload = "<name>" + nameString + "</name>";
                xml name =? <xml>payload;
                res.setXmlPayload(name);
            }
            http:PayloadError payloadError => {
                res.statusCode = 500;
                res.setStringPayload(payloadError.message);

            }
        }
        _ = conn -> respond(res);
    }
}

