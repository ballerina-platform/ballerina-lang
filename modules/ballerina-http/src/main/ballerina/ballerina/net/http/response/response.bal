package ballerina.net.http.response;

import ballerina.doc;

@doc:Description { value:"Gets the HTTP status code from the message"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"int: http status code" }
native function getStatusCode (response res) (int);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"int: length of the message" }
native function getContentLength (response res) (int);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"contentLength: Length of the message" }
native function setContentLength (response res, int contentLength);

@doc:Description { value:"Sets the HTTP StatusCode on the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"statusCode: HTTP status code" }
native function setStatusCode (response res, int statusCode);

@doc:Description { value:"Sets a custom HTTP Reason phrase"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"reasonPhrase: Reason phrase value" }
native function setReasonPhrase (response res, string reasonPhrase);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (response res) (json);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (response res) (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"xml: The XML representation of the message payload" }
native function getXmlPayload (response res) (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
native function getBinaryPayload (response res) (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (response res, string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"payload: The string payload object" }
native function setStringPayload (response res, string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
native function getHeader (response res, string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"string: The string representation of the message payload" }
native function getStringPayload (response res) (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function addHeader (response res, string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string[]: The header values" }
native function getHeaders (response res, string headerName) (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"req: The response message" }
@doc:Param { value:"payload: The JSON payload object" }
native function setJsonPayload (response res, json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
native function getProperty (response res, string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"key: The header name" }
native function removeHeader (response res, string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"res: The response message" }
native function removeAllHeaders (response res);

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"payload: The XML payload object" }
native function setXmlPayload (response res, xml payload);

@doc:Description { value:"Clones and creates a new instance of a message object"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"message: The new instance of the message object " }
native function clone (response res) (message);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function setHeader (response res, string key, string value);

@doc:Description { value:"Sets the message payload using a map object"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"payload: The map payload object" }
native function setMapPayload (response res, map payload);

@doc:Description { value:"To get the value for a string property in a map type message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"propertyName: Name of the property" }
@doc:Return { value:"string: The value of the map property" }
native function getStringValue (response res, string propertyName) (string);
