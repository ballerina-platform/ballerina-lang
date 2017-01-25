package ballerina.net.uri;

native function encode (string url) (string);
native function getQueryParam (message message, string key) (string);