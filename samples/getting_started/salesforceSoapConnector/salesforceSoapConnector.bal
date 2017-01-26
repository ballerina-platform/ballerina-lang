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
        //resp=message:getXmlPayload(response);
        //soapBody = xml:getString(resp, "/soapenv:Envelope/soapenv:Body",{"soapenv" : "http://schemas.xmlsoap.org/soap/envelope/"});
        //system:println(soapBody);
        return response;
    }
}

connector Salesforce (string url) {

    sample:SoapConnector soapConnector = new sample:SoapConnector("");
    string sessionID;

    action addAccount (Salesforce s, string msg1, string msg2, string soapAction, string url) (message response) {
        message backendServiceReq;
        xml payload;
        payload = `<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Header>
                        <SessionHeader>
                            <sessionId>${sessionID}</sessionId>
                        </SessionHeader>
                    </SOAP-ENV:Header>
                    <SOAP-ENV:Body>
                        <addAccount xmlns="http://soap.sforce.com/schemas/class/ConnectorAutomation">
                            <value1>${msg1}</value1>
                            <value2>${msg2}</value2>
                        </addAccount>
                    </SOAP-ENV:Body>
                </SOAP-ENV:Envelope>`;
        response = sample:SoapConnector.post(soapConnector, payload, "''", url);
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
        sessionID = xml:getString(resp, "/soapenv:Envelope/soapenv:Body/ns:loginResponse/ns:result/ns:sessionId/text()", {"soapenv" :"http://schemas.xmlsoap.org/soap/envelope/", "ns":"urn:partner.soap.sforce.com"});
        return response;
    }
}

function main (string[] args) {
    sample:Salesforce sfConnector = new sample:Salesforce("");
    message login;
    message sfResponse;

    login = sample:Salesforce.login(sfConnector, args[0], args[1], "https://login.salesforce.com/services/Soap/u/27.0");
    sfResponse = sample:Salesforce.addAccount(sfConnector, args[2], args[3], "''", "https://ap2.salesforce.com/services/Soap/class/ConnectorAutomation");

    system:println(xml:toString(message:getXmlPayload(sfResponse)));
}