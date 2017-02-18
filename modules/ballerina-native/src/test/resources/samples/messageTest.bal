import ballerina.lang.messages;
import ballerina.lang.system;

function testGetJSONPayload(message msg) (json){
    return messages:getJsonPayload(msg);
}

function testSetJSONPayload(message msg, json payload) (message){
    messages:setJsonPayload(msg, payload);
    return msg;
}

function testSetHeader(message msg, string header, string value) (message){
    messages:setHeader(msg, header, value);
    return msg;
}

function testAddHeader (message msg, string header, string value) (message) {
    messages:addHeader(msg, header, value);
    return msg;
}

function testRemoveHeader (message msg, string header) (message) {
    messages:removeHeader(msg, header);
    return msg;
}

function testSetXmlPayload (message msg, xml payload) (message) {
    messages:setXmlPayload(msg, payload);
    return msg;
}

function testGetHeader(message msg, string header) (string){
    return messages:getHeader(msg, header);
}

function testSetStringPayload(message msg, string payload) (message){
    messages:setStringPayload(msg, payload);
    return msg;
}

function testGetStringPayload(message msg) (message){
    messages:getStringPayload(msg);
    return msg;
}

function testEmptyString() (string){
    message msg = {};
    string strPayload;
    strPayload = messages:getStringPayload(msg);
    return strPayload;
}

function testClone(message msg, string payload2) (int) {
    message clone;
    string v1;
    string v2;
    int state;

    state = 0;
    clone = messages:clone(msg);
    messages:setStringPayload(clone, payload2);

    v1 = messages:getStringPayload(msg);
    v2 = messages:getStringPayload(clone);
    system:log(3, v1);
    system:log(3, v2);
    if( v1 != payload2 ) {
        state = 1;
    } else {
        state = 2;
    }
    return state;
}

function testGetStringValue(message msg, string s) (string){
    return messages:getStringValue(msg, s);
}
