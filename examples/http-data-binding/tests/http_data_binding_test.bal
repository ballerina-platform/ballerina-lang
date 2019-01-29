import ballerina/http;
import ballerina/test;

@test:Config
function testFunc() returns error? {
    http:Client httpEndpoint = new("http://localhost:9090");

    json jsonPayload = { "Details": { "ID": "77999", "Name": "XYZ"} , "Location": { "No": "01", "City": "Colombo"}};
    json expectedJson = {"ID":"77999","Name":"XYZ"};
    var response = httpEndpoint->post("/hello/bindJson", jsonPayload);
    if (response is http:Response) {
        json actualPayload = check response.getJsonPayload();
        test:assertEquals(actualPayload, expectedJson);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    xml xmlPayload = xml `<h:Store id ="AST" xmlns:h="http://www.test.com"><h:street>Main</h:street><h:city>94</h:city></h:Store>`;
    xml expectedXml = xml `<h:city xmlns:h="http://www.test.com">94</h:city>`;
    response = httpEndpoint->post("/hello/bindXML", xmlPayload);
    if (response is http:Response) {
        xml actualPayload = check response.getXmlPayload();
        test:assertEquals(actualPayload, expectedXml);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    jsonPayload = { "Name": "John", "Grade": 12, "Marks": {"English" : "85", "IT" : "100"}};
    expectedJson = {"Name":"John","Grade":12};
    response = httpEndpoint->post("/hello/bindStruct", jsonPayload);
    if (response is http:Response) {
        json actualPayload = check response.getJsonPayload();
        test:assertEquals(actualPayload, expectedJson);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
    return;
}
