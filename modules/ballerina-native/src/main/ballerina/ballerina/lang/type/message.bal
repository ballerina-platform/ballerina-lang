package ballerina.lang.type;

import ballerina.lang.messages;

typemapper string2message (string payload) (message) {
    message m = {};
    messages:setStringPayload (m, payload);
    return m;
}

typemapper xml2message (xml payload) (message) {
    message m = {};
    messages:setXmlPayload (m, payload);
    return m;
}

typemapper json2message (json payload) (message) {
    message m = {};
    messages:setJsonPayload (m, payload);
    return m;
}
