package ballerina.net.http;

import ballerina.doc;

struct Request {
}

struct Response {
}

connector ClientConnector (string serviceUri) {

	@doc:Description { value:"The POST action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action post (string path, request req) (response);

	@doc:Description { value:"The HEAD action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action head (string path, request req) (response);

	@doc:Description { value:"The PUT action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action put (string path, request req) (response);

	@doc:Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@doc:Param { value:"httpVerb: HTTP verb value" }
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action execute (string httpVerb, string path, request req) (response);

	@doc:Description { value:"The PATCH action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action patch (string path, request req) (response);

	@doc:Description { value:"The DELETE action implementation of the HTTP connector"}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action delete (string path, request req) (response);

	@doc:Description { value:"GET action implementation of the HTTP Connector"}
	@doc:Param { value:"path: Request path" }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message object" }
	native action get (string path, request req) (response);
}