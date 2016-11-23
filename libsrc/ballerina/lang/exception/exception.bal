package ballerina.lang.exception;

const string TYPE_IO_EXCEPTION = "IO_EXCEPTION";

native function getCategory(exception e) (string);
native function setCategory(exception e, string s);

native function getMessage(exception e) (string);
native function setMessage(exception e, string s);

native function getProperties(exception e) (map);

