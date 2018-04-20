
import ballerina/doc;

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
native function getJsonPayload (message m) (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"xml: The XML representation of the message payload" }
native function getXmlPayload (message m) (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
native function getBinaryPayload (message m) (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"msg: The current message object" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
native function setProperty (message msg, string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The string payload object" }
native function setStringPayload (message m, string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
native function getHeader (message m, string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"string: The string representation of the message payload" }
native function getStringPayload (message m) (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function addHeader (message m, string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string[]: The header values" }
native function getHeaders (message m, string headerName) (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The JSON payload object" }
native function setJsonPayload (message m, json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"msg: The current message object" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
native function getProperty (message msg, string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
native function removeHeader (message m, string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"m: The message object" }
native function removeAllHeaders (message m);

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The XML payload object" }
native function setXmlPayload (message m, xml payload);

@doc:Description { value:"Clones and creates a new instance of a message object"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"message: The new instance of the message object " }
native function clone (message m) (message);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
native function setHeader (message m, string key, string value);

@doc:Description { value:"Sets the message payload using a map object"}
@doc:Param { value:"msg: The current message object" }
@doc:Param { value:"payload: The map payload object" }
native function setMapPayload (message msg, map payload);

@doc:Description { value:"To get the value for a string property in a map type message"}
@doc:Param { value:"message: message" }
@doc:Param { value:"propertyName: Name of the property" }
@doc:Return { value:"string: The value of the map property" }
native function getStringValue (message m, string propertyName) (string);

