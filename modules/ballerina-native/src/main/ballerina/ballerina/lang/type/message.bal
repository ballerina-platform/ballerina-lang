package ballerina.lang.type;

import ballerina.lang.message;

typemapper string2message (string payload) (message) {
    message m = {};
    message:setStringPayload (m, payload);
    return m;
}

typemapper xml2message (xml payload) (message) {
    message m = {};
    message:setXmlPayload (m, payload);
    return m;
}

typemapper json2message (json payload) (message) {
    message m = {};
    message:setJsonPayload (m, payload);
    return m;
}
