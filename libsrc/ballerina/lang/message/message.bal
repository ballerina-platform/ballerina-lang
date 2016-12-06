package ballerina.lang.message;

// validate these methods with the servlet spec
native function getHeader (message m, string key) (string[]);
native function setHeader (message m, string key, string value);
native function addHeader (message m, string key, string value);

native function getByteArrayPayload (message m) (byte[]);
native function setByteArrayPayload (message m, byte[] payload);

native function getStringPayload (message m) (string);
native function setStringPayload (message m, string payload);

native function getXmlPayload (message m) (xml);
native function setXmlPayload (message m, xml payload);

native function getXmlDocumentPayload (message m) (xmlDocument);
native function setXmlDocumentPayload (message m, xmlDocument payload);

native function getJsonPayload (message m) (json);
native function setJsonPayload (message m, json payload);

native function getMapPayload (message m) (map);
native function setMapPayload (message m, map payload);

native function clone (message m) (message);
