package ballerina.lang.exception;

const string CATEGORY_IO = "IO_EXCEPTION";

native function create (string category, string message, map properties) (exception);

native function getCategory (exception e) (string);
native function setCategory (exception e, string s);

native function getMessage (exception e) (string);
native function setMessage (exception e, string s);

native function getProperties (exception e) (map);
