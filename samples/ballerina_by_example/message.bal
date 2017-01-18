import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.lang.json;
import ballerina.lang.xml;

function main (string[] args) {

    message jsonMsg;
    message stringMsg;
    message xmlMsg;
    message clone;
    json jsonPayload;
    string strPayload;
    xml xmlPayload;

    jsonPayload = `{"name" : "Jack", "streetName" : "WSO2"}`;
    strPayload = "Hello World...!!!";
    xmlPayload = `<person><name>Jack</name></person>`;

    //set a json payload to a message.
    message:setJsonPayload(jsonMsg, jsonPayload);

    //get a json payload from a message.
    system:println((json:toString(message:getJsonPayload(jsonMsg))));

    //set a header to a message.
    message:setHeader(jsonMsg, "Country", "Sri Lanka");

    //get a header from a message.
    system:println(message:getHeader(jsonMsg, "Country"));

    //set a string payload to a message.
    message:setStringPayload(stringMsg, strPayload);

    //get a string payload from a message.
    system:println(message:getStringPayload(stringMsg));

    //set a xml payload to a message.
    message:setXmlPayload(xmlMsg, xmlPayload);

    //get a xml payload from a message.
    system:println(xml:toString(message:getXmlPayload(xmlMsg)));
}