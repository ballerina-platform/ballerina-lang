package org.wso2.ballerina.sample;

import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.xml;


connector SoapConnector (string url) {

    http:HTTPConnector httpConnector = new http:HTTPConnector("");

    action post (SoapConnector s, xml payload, string soapAction, string url) (message response) {
        message backendServiceReq;

        string soapBody;
        xml resp;
        message:setXmlPayload(backendServiceReq, payload);
        message:setHeader(backendServiceReq, "Content-Type", "text/xml");
        message:setHeader(backendServiceReq, "SOAPAction", soapAction);

        response = http:HTTPConnector.post(httpConnector, url, backendServiceReq);
        return response;
    }
}

connector Salesforce (string url) {

    sample:SoapConnector soapConnector = new sample:SoapConnector("");
    string sessionID;
    string serviceUrl;

    action addAccount (Salesforce s, string msg1, string msg2, string soapAction, string url) (message response) {
        message backendServiceReq;
        xml payload;
        system:println(sessionID);
        payload = `<soapenv:Envelope
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:urn="urn:partner.soap.sforce.com">
                    <soapenv:Header>
                        <urn:SessionHeader>
                            <urn:sessionId>${sessionID}</urn:sessionId>
                        </urn:SessionHeader>
                    </soapenv:Header>
                    <soapenv:Body>
                        <urn:getUserInfo/>
                    </soapenv:Body>
                </soapenv:Envelope>`;
        response = sample:SoapConnector.post(soapConnector, payload, soapAction, url);
        return response;
    }

    action login (Salesforce s, string username, string password, string url) (message response) {
        message backendServiceReq;
        xml payload;
        string soapBody;
        xml resp;
        payload = `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                          xmlns:urn="urn:partner.soap.sforce.com">
                            <soapenv:Body>
                                <urn:login>
                                    <urn:username>${username}</urn:username>
                                    <urn:password>${password}</urn:password>
                                </urn:login>
                            </soapenv:Body>
                        </soapenv:Envelope>`;
        response = sample:SoapConnector.post(soapConnector, payload, "''", url);
        resp = message:getXmlPayload(response);
        soapBody = xml:getString(resp, "/soapenv:Envelope/soapenv:Body/ns:loginResponse/ns:result/ns:sessionId/text()", {"soapenv" :"http://schemas.xmlsoap.org/soap/envelope/", "ns":"urn:partner.soap.sforce.com"});
        sessionID = soapBody;
        return response;
    }
}

function main (string[] args) {
    sample:Salesforce soapConnector = new sample:Salesforce("");
    message login;
    message sfResponse;

    login = sample:Salesforce.login(soapConnector, args[0], args[1], "https://login.salesforce.com/services/Soap/u/27.0");
    sfResponse = sample:Salesforce.addAccount(soapConnector, args[2], args[3], "urn:partner.soap.sforce.com/Soap/describeGlobalRequest", "https://ap2.salesforce.com/services/Soap/u/27.0/");

    system:println(xml:toString(message:getXmlPayload(sfResponse)));
}