package ballerina.net.http;

import ballerina.doc;

struct request {
}

struct response {
}

@doc:Description { value:"Sets the HTTP StatusCode on the message"}
@doc:Param { value:"m: A message object" }
@doc:Param { value:"statusCode: HTTP status code" }
native function setStatusCode (message m, int statusCode);

@doc:Description { value:"Sets a custom HTTP Reason phrase"}
@doc:Param { value:"m: A message object" }
@doc:Param { value:"reasonPhrase: Reason phrase value" }
native function setReasonPhrase (message m, string reasonPhrase);

connector ClientConnector (string serviceUri) {

	@doc:Description { value:"The POST action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A request message" }
	@doc:Return { value:"message: The response message object" }
	native action post (string path, request req) (message);

	@doc:Description { value:"The HEAD action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A request message" }
	@doc:Return { value:"message: The response message object" }
	native action head (string path, request req) (message);

	@doc:Description { value:"The PUT action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A request message" }
	@doc:Return { value:"message: The response message object" }
	native action put (string path, request req) (message);

	@doc:Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@doc:Param { value:"httpVerb: HTTP verb value" }
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A request message" }
	@doc:Return { value:"message: The response message object" }
	native action execute (string httpVerb, string path, request req) (message);

	@doc:Description { value:"The PATCH action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A request message" }
	@doc:Return { value:"message: The response message object" }
	native action patch (string path, request req) (message);

	@doc:Description { value:"The DELETE action implementation of the HTTP connector"}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"m: A request message" }
	@doc:Return { value:"message: The response message object" }
	native action delete (string path, request req) (message);

	@doc:Description { value:"GET action implementation of the HTTP Connector"}
	@doc:Param { value:"path: Request path" }
	@doc:Param { value:"m: A request message" }
	native action get (string path, request req) (message);

}