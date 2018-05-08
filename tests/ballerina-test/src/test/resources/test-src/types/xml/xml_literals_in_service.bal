import ballerina/http;

xmlns "http://ballerina.com/a" as ns0; 

endpoint http:NonListener testEP {
    port:9090
};

@http:ServiceConfig {basePath:"/test"}
service<http:Service> TestServiceLevelNamespaces bind testEP {
    xmlns "http://ballerina.com/b" as ns1; 
    
    xml person = xml `<p:person xmlns:p="foo" xmlns:q="bar">hello</p:person>`;

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getXML"
    }
    getXML (endpoint conn, http:Request request) {
        http:Response res = new;
        res.setXmlPayload(person);
        _ = conn -> respond(res);
    }
}