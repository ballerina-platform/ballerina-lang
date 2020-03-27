import ballerina/mime;
import ballerina/http;
import ballerina/test;

@test:Config {}
function testFunc() {

    //TODO: Enable the following tests once the relative path issue is fixed. Until then the encoder/decoder should be
    //tested manually.

    //http:Client httpEP1 = new("http://localhost:9090");
    //http:Client httpEP2 = new("http://localhost:9092");
    //
    //var response1 = httpEP1->get("/multiparts/decode_in_response");
    //if (response1 is http:Response) {
    //    var result = response1.getTextPayload();
    //    if (result is string) {
    //        test:assertEquals(result, "Body Parts Received!");
    //    } else {
    //        test:assertFail(msg = "Invalid response body");
    //    }
    //} else if (response1 is error) {
    //    test:assertFail(msg = "Error in calling multipart decoder");
    //}
    //
    //var response2 = httpEP2->get("/multiparts/encode_out_response");
    //if (response2 is http:Response) {
    //    var parentParts = response2.getBodyParts();
    //    if (parentParts is mime:Entity[]) {
    //        var childParts = parentParts[0].getBodyParts();
    //        if (childParts is mime:Entity[]) {
    //            var jsonValue = childParts[0].getJson();
    //            if (jsonValue is json) {
    //                test:assertEquals(jsonValue.toString(), "{\"name\":\"wso2\"}");
    //            } else {
    //                test:assertFail(msg = "Invalid json");
    //            }
    //            var xmlValue = childParts[1].getXml();
    //            if (xmlValue is xml) {
    //                xml element1 = xmlValue.selectDescendants("version");
    //                test:assertEquals(element1.getTextValue(), "0.963");
    //                xml element2 = xmlValue.selectDescendants("test");
    //                test:assertEquals(element2.getTextValue(), "test xml file to be used as a file part");
    //            } else {
    //                test:assertFail(msg = "Invalid xml");
    //            }
    //        } else if (childParts is error){
    //            test:assertFail(msg = "Invalid child parts");
    //        }
    //    } else if (parentParts is error){
    //        test:assertFail(msg = "Invalid parent parts");
    //    }
    //} else if (response2 is error) {
    //    test:assertFail(msg = "Error in calling multipart encoder");
    //}
}
