import ballerina/http;

xmlns "http://ballerina.com/a" as ns0; 
xmlns "http://ballerina.com/b" as ns1;

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {basePath:"/test"}
service TestServiceLevelNamespaces on testEP {

    xml person = xml `<p:person xmlns:p="foo" xmlns:q="bar">hello</p:person>`;

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getXML"
    }

    resource function getXML (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setXmlPayload(self.person);
        checkpanic caller->respond(res);
    }
}
