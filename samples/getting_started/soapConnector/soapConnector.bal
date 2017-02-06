package org.wso2.ballerina.sample;

import ballerina.lang.message;
import ballerina.net.http;
import ballerina.lang.system;
import ballerina.lang.string;
import ballerina.lang.xml;


connector SoapConnector (string url) {

    http:HTTPConnector httpConnector = new http:HTTPConnector("");

    action send (SoapConnector s, xml payload, string soapAction, string url, string soapVersion) (message) {
        string namespace;
        message backendServiceReq;
        xml soapPayload;
        xml resp;
        message response;
        string reqType;
        string soapBody;

        if (string:equalsIgnoreCase(soapVersion, "1.2")) {
            reqType = "application/soap+xml";
            namespace = "http://www.w3.org/2003/05/soap-envelope";
        } else {
            reqType = "text/xml";
            namespace = "http://schemas.xmlsoap.org/soap/envelope/";
        }

        soapPayload = addSoapBody (payload, namespace);

        message:setXmlPayload(backendServiceReq, soapPayload);
        message:setHeader(backendServiceReq, "Content-Type", reqType);
        if (soapAction != "null") {
            message:setHeader(backendServiceReq, "SOAPAction", soapAction);
        }
        response = http:HTTPConnector.post(httpConnector, url, backendServiceReq);
        resp = message:getXmlPayload(response);

        if (string:equalsIgnoreCase(soapVersion, "1.2")) {
            soapBody = xml:getString(resp, "/soapenv:Envelope/soapenv:Body/*", {"soapenv" :"http://www.w3.org/2003/05/soap-envelope"});
        } else {
            soapBody = xml:getString(resp, "/soapenv:Envelope/soapenv:Body/*", {"soapenv" :"http://schemas.xmlsoap.org/soap/envelope/"});
        }

        message:setStringPayload (backendServiceReq, soapBody);
        return backendServiceReq;
    }

}

function addSoapBody (xml payload, string namespace) (xml) {
    xml soapBody;
    soapBody = `<soapenv:Envelope xmlns:soapenv="${namespace}">
	<soapenv:Body>
	${payload}
	</soapenv:Body>
	</soapenv:Envelope>`;
    return soapBody;
}

function main (string[] args) {
    sample:SoapConnector soapConnector = new sample:SoapConnector("");
    xml payload;
    message soapResponse;
    xml soapXMLResponse;
    string sessionId;
    string username;
    string password;

    username = args[0];
    password = args[1];
    payload = `<urn:login xmlns:urn="urn:partner.soap.sforce.com">
	<urn:username>${username}</urn:username>
	<urn:password>${password}</urn:password>
	</urn:login>`;
    soapResponse = sample:SoapConnector.send(soapConnector, payload, "''", "https://login.salesforce.com/services/Soap/u/27.0", "1.1");

    soapXMLResponse = message:getXmlPayload(soapResponse);
    sessionId = xml:getString(soapXMLResponse, "/ns:loginResponse/ns:result/ns:sessionId/text()", {"ns":"urn:partner.soap.sforce.com"});
    system:println(sessionId);
}

