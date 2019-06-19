import ballerina/mime;
import ballerina/http;
import ballerina/test;

@test:Config
function testFunc() {
    http:Client httpEP = new("http://localhost:9090");

    mime:Entity jsonBodyPart = new;
    jsonBodyPart.setJson({ "name": "ballerina" });
    mime:Entity[] bodyParts = [jsonBodyPart];

    http:Request request = new;
    request.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);

    var response1 = httpEP->post("/multiparts/decode", request);
    if (response1 is http:Response) {
        var result = response1.getBodyParts();
        if (result is mime:Entity[]) {
            var jsonValue = result[0].getJson();
            if (jsonValue is json) {
                test:assertEquals(jsonValue.toString(), "{\"name\":\"ballerina\"}");
            } else {
                test:assertFail(msg = "Invalid json");
            }
        } else {
            test:assertFail(msg = "Invalid body parts");
        }
    } else {
        test:assertFail(msg = "Error in calling multipart decoder");
    }

    //TODO: Enable the following test once the relative path issue is fixed. Until then the encoder should be tested
    //manually.
    //var response2 = httpEP->get("/multiparts/encode");
    //if (response2 is http:Response) {
    //    var result = response2.getBodyParts();
    //    if (result is mime:Entity[]) {
    //        var jsonValue = result[0].getJson();
    //        if (jsonValue is json) {
    //            test:assertEquals(jsonValue.toString(), "{\"name\":\"wso2\"}");
    //        } else {
    //            test:assertFail(msg = "Invalid json");
    //        }
    //        var xmlValue = result[1].getXml();
    //        if (xmlValue is xml) {
    //            xml element1 = xmlValue.selectDescendants("version");
    //            test:assertEquals(element1.getTextValue(), "0.963");
    //            xml element2 = xmlValue.selectDescendants("test");
    //            test:assertEquals(element2.getTextValue(), "test xml file to be used as a file part");
    //        } else {
    //            test:assertFail(msg = "Invalid xml");
    //        }
    //    } else if (result is error){
    //        test:assertFail(msg = "Invalid body parts");
    //    }
    //} else if (response2 is error) {
    //    test:assertFail(msg = "Error in calling multipart encoder");
    //}
}
