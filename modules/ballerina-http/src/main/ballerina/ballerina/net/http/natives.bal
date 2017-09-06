package ballerina.net.http;

import ballerina.doc;

@doc:Description { value:"Gets the request URL from the message"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"string: The request URL value" }
native function getRequestURL (message m) (string);

@doc:Description { value:"Gets the HTTP status code from the message"}
@doc:Param { value:"m: A message object" }
@doc:Return { value:"int: http status code" }
native function getStatusCode (message m) (int);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"m: A message object" }
@doc:Return { value:"int: length of the message" }
native function getContentLength (message m) (int);

@doc:Description { value:"Gets the HTTP method from the message"}
@doc:Param { value:"m: A message object" }
@doc:Return { value:"string: http method value" }
native function getMethod (message m) (string);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"m: A message object" }
@doc:Param { value:"contentLength: Length of the message" }
native function setContentLength (message m, int contentLength);

@doc:Description { value:"Converts the message into an HTTP response"}
@doc:Param { value:"m: A message object" }
native function convertToResponse (message m);

@doc:Description { value:"Sets the HTTP StatusCode on the message"}
@doc:Param { value:"m: A message object" }
@doc:Param { value:"statusCode: HTTP status code" }
native function setStatusCode (message m, int statusCode);

@doc:Description { value:"Sets a custom HTTP Reason phrase"}
@doc:Param { value:"m: A message object" }
@doc:Param { value:"reasonPhrase: Reason phrase value" }
native function setReasonPhrase (message m, string reasonPhrase);

@doc:Description { value:"Gets formParam map from HTTP message"}
@doc:Param { value:"m: The message object" }
@doc:Return { value:"map: The map of form params" }
native function getFormParams (message m) (map);

connector ClientConnector (string serviceUri) {

	@doc:Description { value:"The POST action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A message object" }
	@doc:Return { value:"message: The response message object" }
	native action post (string path, message m) (message);

	@doc:Description { value:"The HEAD action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A message object" }
	@doc:Return { value:"message: The response message object" }
	native action head (string path, message m) (message);

	@doc:Description { value:"The PUT action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A message object" }
	@doc:Return { value:"message: The response message object" }
	native action put (string path, message m) (message);

	@doc:Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@doc:Param { value:"httpVerb: HTTP verb value" }
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A message object" }
	@doc:Return { value:"message: The response message object" }
	native action execute (string httpVerb, string path, message m) (message);

	@doc:Description { value:"The PATCH action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A message object" }
	@doc:Return { value:"message: The response message object" }
	native action patch (string path, message m) (message);

	@doc:Description { value:"The DELETE action implementation of the HTTP connector"}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A message object" }
	@doc:Return { value:"message: The response message object" }
	native action delete (string path, message m) (message);

	@doc:Description { value:"GET action implementation of the HTTP Connector"}
	@doc:Param { value:"path: Request path" }
	@doc:Param { value:"m: message" }
	native action get (string path, message m) (message);

}