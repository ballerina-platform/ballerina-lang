import ballerina.net.http;

endpoint<http:Service> infoServiceEP {
    port:9092
}

@Description {value:"Consumes and Produces annotations contain MIME types as an array of strings."}
@http:serviceConfig { endpoints:[infoServiceEP] }
service<http:Service> infoService {

    @http:resourceConfig {
        methods:["POST"],
        path:"/",
        consumes:["text/json", "application/json"],
        produces:["application/xml"]
    }
    @Description {value:"Resource can consume/accept text/json and application/json media types only. Therefore Content-Type header must have one of the types."}
    @Description {value:"Resource can produce application/xml payloads. Therefore Accept header should be set accordingly."}
    resource student (http:ServerConnector conn, http:Request req) {
        //Get JSON payload from the request message.
        var jsonMsg, payloadError = req.getJsonPayload();
        http:Response res = {};
        if (payloadError == null) {
            //Get the string value relevant to the key "name".
            string nameString;
            nameString, _ = (string)jsonMsg["name"];
            //Create XML payload and respond back.
            string payload = "<name>" + nameString + "</name>";
            var name, _ = <xml>payload;
            res.setXmlPayload(name);
        } else {
            res.statusCode = 500;
            res.setStringPayload(payloadError.message);
        }

        _ = conn -> respond(res);
    }
}

