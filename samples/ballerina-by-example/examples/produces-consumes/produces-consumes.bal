import ballerina.net.http;
import ballerina.lang.xmls;
import ballerina.lang.messages;
import ballerina.doc;
@doc:Description {value:"Consumes and Produces annotations contain MIME types as an array of strings."}
service<http> infoService {
    @http:POST {}
    @http:Path {value:"/"}
    @doc:Description {value:"Resource can consume/accept text/json and application/json media types only. Therefore Content-Type header must have one of the types."}
    @http:Consumes{value:["text/json","application/json"]}
    @doc:Description {value:"Resource can produce application/xml payloads. Therefore Accept header should be set accordingly."}
    @http:Produces{value:["application/xml"]}
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
        reply response;
    }
}

