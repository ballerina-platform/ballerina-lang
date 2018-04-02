import ballerina/http;
import ballerina/lang.xmls;
import ballerina/lang.messages;
import ballerina/doc;
@doc:Description {value:"Consumes and Produces annotations contain MIME types as an array of strings."}
service<http> infoService {

    @http:resourceConfig {
        methods:["POST"],
        path:"/",
        consumes:["text/json","application/json"],
        produces:["application/xml"]
    }
    @doc:Description {value:"Resource can consume/accept text/json and application/json media types only. Therefore Content-Type header must have one of the types."}
    @doc:Description {value:"Resource can produce application/xml payloads. Therefore Accept header should be set accordingly."}
    resource student (message m) {
        //Get JSON payload from the request message.
        json jsonMsg = messages:getJsonPayload(m);
        //Get the string value relevant to the key "name".
        string nameString;
        nameString, _ = (string)jsonMsg["name"];
        message response = {};
        //Create XML payload and respond back.
        xml name = xmls:parse("<name>"+nameString+"</name>");
        messages:setXmlPayload(response,name);
        response:send(response);
    }
}

