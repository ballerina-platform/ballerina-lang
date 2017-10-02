package ballerina.net.http;

import ballerina.doc;

struct Request {
}

struct Response {
}

struct Options{
    boolean followRedirect = false;
    int maximumRedirectCount = 5;
	string trustStoreFile;
	string trustStorePassword;
	string keyStoreFile;
	string keyStorePassword;
}

connector ClientConnector (string serviceUri, Options connectorOptions) {

	@doc:Description { value:"The POST action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action post (string path, Request req) (Response);

	@doc:Description { value:"The HEAD action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action head (string path, Request req) (Response);

	@doc:Description { value:"The PUT action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action put (string path, Request req) (Response);

	@doc:Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@doc:Param { value:"httpVerb: HTTP verb value" }
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action execute (string httpVerb, string path, Request req) (Response);

	@doc:Description { value:"The PATCH action implementation of the HTTP Connector."}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action patch (string path, Request req) (Response);

	@doc:Description { value:"The DELETE action implementation of the HTTP connector"}
	@doc:Param { value:"path: Resource path " }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action delete (string path, Request req) (Response);

	@doc:Description { value:"GET action implementation of the HTTP Connector"}
	@doc:Param { value:"path: Request path" }
	@doc:Param { value:"req: A request message" }
	@doc:Return { value:"response: The response message" }
	native action get (string path, Request req) (Response);
}