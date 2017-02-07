import ballerina.lang.message;
import ballerina.lang.system;

function testGetJSONPayload(message msg) (json){
    return message:getJsonPayload(msg);
}

function testSetJSONPayload(message msg, json payload) (message){
    message:setJsonPayload(msg, payload);
    return msg;
}

function testSetHeader(message msg, string header, string value) (message){
    message:setHeader(msg, header, value);
    return msg;
}

function testGetHeader(message msg, string header) (string){
    return message:getHeader(msg, header);
}

function testSetStringPayload(message msg, string payload) (message){
    message:setStringPayload(msg, payload);
    return msg;
}

function testGetStringPayload(message msg) (message){
    message:getStringPayload(msg);
    return msg;
}

function testEmptyString() (string){
    message msg = {};
    string strPayload;
    strPayload = message:getStringPayload(msg);
    return strPayload;
}

function testClone(message msg, string payload2) (int) {
    message clone;
    string v1;
    string v2;
    int state;

    state = 0;
    clone = message:clone(msg);
    message:setStringPayload(clone, payload2);

    v1 = message:getStringPayload(msg);
    v2 = message:getStringPayload(clone);
    system:log(3, v1);
    system:log(3, v2);
    if( v1 != payload2 ) {
     state = 1;
    } else {
     state = 2;
    }
    return state;
}
