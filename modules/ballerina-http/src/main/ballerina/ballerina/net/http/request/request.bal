package ballerina.net.http.request;

import ballerina.doc;
import ballerina.net.http;

@doc:Description { value:"Gets the request URL from the message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"string: The request URL value" }
native function getRequestURL (http:Request req) (string);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"int: length of the message" }
native function getContentLength (http:Request req) (int);

@doc:Description { value:"Gets the HTTP method from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: http method value" }
native function getMethod (http:Request req) (string);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"contentLength: Length of the message" }
native function setContentLength (http:Request req, int contentLength);

@doc:Description { value:"Gets formParam map from HTTP message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"map: The map of form params" }
native function getFormParams (http:Request req) (map);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (http:Request req) (json);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (http:Request req) (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"xml: The XML representation of the message payload" }
native function getXmlPayload (http:Request req) (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
native function getBinaryPayload (http:Request req) (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (http:Request req, string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The string payload object" }
native function setStringPayload (http:Request req, string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
native function getHeader (http:Request req, string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: The string representation of the message payload" }
native function getStringPayload (http:Request req) (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function addHeader (http:Request req, string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string[]: The header values" }
native function getHeaders (http:Request req, string headerName) (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The JSON payload object" }
native function setJsonPayload (http:Request req, json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
native function getProperty (http:Request req, string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
native function removeHeader (http:Request req, string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"m: The message object" }
native function removeAllHeaders (http:Request req);

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The XML payload object" }
native function setXmlPayload (http:Request req, xml payload);

@doc:Description { value:"Clones and creates a new instance of a message object"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"message: The new instance of the message object " }
native function clone (http:Request req) (message);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function setHeader (http:Request req, string key, string value);

@doc:Description { value:"To get the value for a string property in a map type message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: Name of the property" }
@doc:Return { value:"string: The value of the map property" }
native function getStringValue (http:Request req, string propertyName) (string);
