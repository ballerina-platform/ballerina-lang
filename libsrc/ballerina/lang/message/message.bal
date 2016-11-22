package ballerina.lang.message;

native function getHeader(message m, string key) (string[]);

native function getPayload(message m) (byte[]);
native function setPayload(message m, byte[] bytes);

native function getPayload(message m) (string);
native function setPayload(message m, string s);

native function getPayload(message m) (xmlElement);
native function setPayload(message m, xmlElement x);

native function getPayload(message m) (json);
native function setPayload(message m, json j);

native function clone(message m) (message);





