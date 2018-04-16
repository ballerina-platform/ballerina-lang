import ballerina/http;
import ballerina/mime;
endpoint http:Listener infoServiceEP {
    port:9092
};

@Description {value:"Consumes and produces annotations that contain MIME types as an array of strings."}
@http:ServiceConfig {basePath:"infoService"}
@Description {value:"The resource can consume/accept `text/json` and `application/json` media types only. Therefore, the `Content-Type` header must have one of the types."}
service<http:Service> infoService bind infoServiceEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/",
        consumes:["text/json", "application/json"],
        produces:["application/xml"]
    }
    @Description {value:"The resource can produce `application/xml` payloads. Therefore, the `Accept` header should be set accordingly."}
    student (endpoint conn, http:Request req) {
        //Get JSON payload from the request message.
        http:Response res = new;
        var msg = req.getJsonPayload();
        match msg {
            json jsonMsg => {
            //Get the string value that is relevant to the key "name".
                string nameString = check <string>jsonMsg["name"];
                //Create XML payload and send back a response. 
                string payload = "<name>" + nameString + "</name>";
                xml name = check <xml>payload;
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

