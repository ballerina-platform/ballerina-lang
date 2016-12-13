package ballerina.lang.type;

import ballerina.lang.message;

typeconvertor string2message (string payload) (message) {
    message m;

    m = new message ();
    message:setStringPayload (m, payload);
    return m;
}

typeconvertor xml2message (xml payload) (message) {
    message m;

    m = new message ();
    message:setXmlPayload (m, payload);
    return m;
}

typeconvertor json2message (json payload) (message) {
    message m;

    m = new message ();
    message:setJsonPayload (m, payload);
    return m;
}
