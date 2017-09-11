package ballerina.net.http.request;

import ballerina.doc;

@doc:Description { value:"Gets the request URL from the message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"string: The request URL value" }
native function getRequestURL (Request req) (string);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"int: length of the message" }
native function getContentLength (Request req) (int);

@doc:Description { value:"Gets the HTTP method from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: http method value" }
native function getMethod (Request req) (string);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"contentLength: Length of the message" }
native function setContentLength (Request req, int contentLength);

@doc:Description { value:"Sets a custom HTTP Reason phrase"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"reasonPhrase: Reason phrase value" }
native function setReasonPhrase (Request req, string reasonPhrase);

@doc:Description { value:"Gets formParam map from HTTP message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"map: The map of form params" }
native function getFormParams (Request req) (map);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (Request req) (json);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (Request req) (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"xml: The XML representation of the message payload" }
native function getXmlPayload (Request req) (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
native function getBinaryPayload (Request req) (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (Request req, string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The string payload object" }
native function setStringPayload (Request req, string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
native function getHeader (Request req, string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: The string representation of the message payload" }
native function getStringPayload (Request req) (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function addHeader (Request req, string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string[]: The header values" }
native function getHeaders (Request req, string headerName) (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The JSON payload object" }
native function setJsonPayload (Request req, json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
native function getProperty (Request req, string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
native function removeHeader (Request req, string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"m: The message object" }
native function removeAllHeaders (Request req);

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The XML payload object" }
native function setXmlPayload (Request req, xml payload);

@doc:Description { value:"Clones and creates a new instance of a message object"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"message: The new instance of the message object " }
native function clone (Request req) (message);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function setHeader (Request req, string key, string value);

@doc:Description { value:"Sets the message payload using a map object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The map payload object" }
native function setMapPayload (Request req, map payload);

@doc:Description { value:"To get the value for a string property in a map type message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: Name of the property" }
@doc:Return { value:"string: The value of the map property" }
native function getStringValue (Request req, string propertyName) (string);
