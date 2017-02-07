function testGetJSONPayload(message msg) (json){
    return ballerina.lang.message:getJsonPayload(msg);
}

function testSetJSONPayload(message msg, json payload) (message){
    ballerina.lang.message:setJsonPayload(msg, payload);
    return msg;
}

function testSetHeader(message msg, string header, string value) (message){
    ballerina.lang.message:setHeader(msg, header, value);
    return msg;
}

function testGetHeader(message msg, string header) (string){
    return ballerina.lang.message:getHeader(msg, header);
}

function testSetStringPayload(message msg, string payload) (message){
    ballerina.lang.message:setStringPayload(msg, payload);
    return msg;
}

function testGetStringPayload(message msg) (message){
    ballerina.lang.message:getStringPayload(msg);
    return msg;
}

function testEmptyString() (string){
    message msg;
    string strPayload;
    msg = new message;
    strPayload = ballerina.lang.message:getStringPayload(msg);
    return strPayload;
}

function testClone(message msg, string payload2) (int) {
    message clone;
    string v1;
    string v2;
    int state;

    state = 0;
    clone = ballerina.lang.message:clone(msg);
    ballerina.lang.message:setStringPayload(clone, payload2);

    v1 = ballerina.lang.message:getStringPayload(msg);
    v2 = ballerina.lang.message:getStringPayload(clone);
    ballerina.lang.system:log(3, v1);
    ballerina.lang.system:log(3, v2);
    if( v1 != payload2 ) {
     state = 1;
    } else {
     state = 2;
    }
    return state;
}
