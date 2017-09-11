package ballerina.net.http.request;

import ballerina.doc;

@doc:Description { value:"Gets the request URL from the message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"string: The request URL value" }
native function getRequestURL (request req) (string);

@doc:Description { value:"Gets the HTTP status code from the message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"int: http status code" }
native function getStatusCode (request req) (int);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"int: length of the message" }
native function getContentLength (request req) (int);

@doc:Description { value:"Gets the HTTP method from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: http method value" }
native function getMethod (request req) (string);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"contentLength: Length of the message" }
native function setContentLength (request req, int contentLength);

@doc:Description { value:"Converts the message into an HTTP response"}
@doc:Param { value:"m: A request message" }
native function convertToResponse (request req);

@doc:Description { value:"Sets the HTTP StatusCode on the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"statusCode: HTTP status code" }
native function setStatusCode (request req, int statusCode);

@doc:Description { value:"Sets a custom HTTP Reason phrase"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"reasonPhrase: Reason phrase value" }
native function setReasonPhrase (request req, string reasonPhrase);

@doc:Description { value:"Gets formParam map from HTTP message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"map: The map of form params" }
native function getFormParams (request req) (map);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (request req) (json);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (request req) (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"xml: The XML representation of the message payload" }
native function getXmlPayload (request req) (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
native function getBinaryPayload (request req) (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (request req, string propertyName, string propertyValue);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (request req, string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The string payload object" }
native function setStringPayload (request req, string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
native function getHeader (request req, string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: The string representation of the message payload" }
native function getStringPayload (request req) (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function addHeader (request req, string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string[]: The header values" }
native function getHeaders (request req, string headerName) (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The JSON payload object" }
native function setJsonPayload (request req, json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
native function getProperty (request req, string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
native function removeHeader (request req, string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"m: The message object" }
native function removeAllHeaders (request req);

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The XML payload object" }
native function setXmlPayload (request req, xml payload);

@doc:Description { value:"Clones and creates a new instance of a message object"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"message: The new instance of the message object " }
native function clone (message m) (message);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function setHeader (request req, string key, string value);

@doc:Description { value:"Sets the message payload using a map object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The map payload object" }
native function setMapPayload (request req, map payload);

@doc:Description { value:"To get the value for a string property in a map type message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: Name of the property" }
@doc:Return { value:"string: The value of the map property" }
native function getStringValue (request req, string propertyName) (string);
