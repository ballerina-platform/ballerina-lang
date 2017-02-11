package ballerina.lang.message;

native function getHeader (message m, string key) (string);

native function setHeader (message m, string key, string value);
native function addHeader (message m, string key, string value);
native function removeHeader (message m, string key);

native function getStringPayload (message m) (string);
native function setStringPayload (message m, string payload);

native function getXmlPayload (message m) (xml);
native function setXmlPayload (message m, xml payload);

native function getJsonPayload (message m) (json);
native function setJsonPayload (message m, json payload);

native function getStringValue (message m, string key) (string);
