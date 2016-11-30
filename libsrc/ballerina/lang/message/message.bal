package ballerina.lang.message;

// validate these methods with the servlet spec
native function getHeader(message m, string key) (string[]);
native function setHeader(message m, string key, string value);
native function addHeader(message m, string key, string value);

native function getPayload(message m) (byte[]);
native function setPayload(message m, byte[] payload);

native function getPayload(message m) (string);
native function setPayload(message m, string payload);

native function getPayload(message m) (xmlElement);
native function setPayload(message m, xmlElement payload);

native function getPayload(message m) (json);
native function setPayload(message m, json payload);

native function getPayload(message m) (map);
native function setPayload(message m, map payload);

native function clone(message m) (message);
