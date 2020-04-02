import ballerina/http;

xmlns "http://ballerina.com/a" as ns0; 
xmlns "http://ballerina.com/b" as ns1;

listener http:MockListener testEP = new(9091);

@http:ServiceConfig {basePath:"/test"}
service TestServiceLevelNamespaces on testEP {

    xml person = xml `<employees>
                        <employee id="1">
                          <name>Parakum</name>
                          <age>32</age>
                          <address>
                              <line1>King's palace</line1>
                              <line2>Polonnaruwa</line2>
                          </address>
                        </employee>
                        <employee id="2">
                          <name>Kashyapa</name>
                          <age>35</age>
                          <address>
                              <line1>Rock palace</line1>
                              <line2>Sigiriya</line2>
                          </address>
                        </employee>
                        <employee id="1">
                            <name>Parakum</name>
                            <age>32</age>
                            <address>
                                <line1>King's palace</line1>
                                <line2>Polonnaruwa</line2>
                            </address>
                        </employee>
                        <employee id="2">
                            <name>Kashyapa</name>
                            <age>35</age>
                            <address>
                                <line1>Rock palace</line1>
                                <line2>Sigiriya</line2>
                            </address>
                        </employee>
                        <employee id="1">
                          <name>Parakum</name>
                          <age>32</age>
                          <address>
                              <line1>King's palace</line1>
                              <line2>Polonnaruwa</line2>
                          </address>
                        </employee>
                        <employee id="2">
                          <name>Kashyapa</name>
                          <age>35</age>
                          <address>
                              <line1>Rock palace</line1>
                              <line2>Sigiriya</line2>
                          </address>
                        </employee>
                        <employee id="1">
                            <name>Parakum</name>
                            <age>32</age>
                            <address>
                                <line1>King's palace</line1>
                                <line2>Polonnaruwa</line2>
                            </address>
                        </employee>
                        <employee id="2">
                            <name>Kashyapa</name>
                            <age>35</age>
                            <address>
                                <line1>Rock palace</line1>
                                <line2>Sigiriya</line2>
                            </address>
                        </employee>
                        <employee id="2">
                          <name>Kashyapa</name>
                          <age>35</age>
                          <address>
                              <line1>Rock palace</line1>
                              <line2>Sigiriya</line2>
                          </address>
                        </employee>
                        <employee id="1">
                            <name>Parakum</name>
                            <age>32</age>
                            <address>
                                <line1>King's palace</line1>
                                <line2>Polonnaruwa</line2>
                            </address>
                        </employee>
                        <employee id="2">
                            <name>Kashyapa</name>
                            <age>35</age>
                            <address>
                                <line1>Rock palace</line1>
                                <line2>Sigiriya</line2>
                            </address>
                        </employee>
                        <employee id="2">
                          <name>Kashyapa</name>
                          <age>35</age>
                          <address>
                              <line1>Rock palace</line1>
                              <line2>Sigiriya</line2>
                          </address>
                        </employee>
                        <employee id="1">
                            <name>Parakum</name>
                            <age>32</age>
                            <address>
                                <line1>King's palace</line1>
                                <line2>Polonnaruwa</line2>
                            </address>
                        </employee>
                        <employee id="2">
                            <name>Kashyapa</name>
                            <age>35</age>
                            <address>
                                <line1>Rock palace</line1>
                                <line2>Sigiriya</line2>
                            </address>
                        </employee>
                      </employees>`;

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
