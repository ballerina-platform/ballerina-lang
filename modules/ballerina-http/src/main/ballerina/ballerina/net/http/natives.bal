package ballerina.net.http;

import ballerina.doc;

public struct Request {
}

@doc:Description { value:"Gets the request URL from the message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"string: The request URL value" }
public native function <Request req> getRequestURL () (string);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"int: length of the message" }
public native function <Request req> getContentLength () (int);

@doc:Description { value:"Gets the HTTP method from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: http method value" }
public native function <Request req> getMethod () (string);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"contentLength: Length of the message" }
public native function <Request req> setContentLength (int contentLength);

@doc:Description { value:"Gets formParam map from HTTP message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"map: The map of form params" }
public native function <Request req> getFormParams () (map);

@doc:Description { value:"Gets queryParam map from HTTP message"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"map: The map of query params" }
public native function <Request req> getQueryParams () (map);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
public native function <Request req> getJsonPayload () (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"req: The request message" }
@doc:Return { value:"xml: The XML representation of the message payload" }
public native function <Request req> getXmlPayload () (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
public native function <Request req> getBinaryPayload () (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
public native function <Request req> setProperty (string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The string payload object" }
public native function <Request req> setStringPayload (string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
public native function <Request req> getHeader (string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string: The string representation of the message payload" }
public native function <Request req> getStringPayload () (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"m: The message object" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
public native function <Request req> addHeader (string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"string[]: The header values" }
public native function <Request req> getHeaders () (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"payload: The JSON payload object" }
public native function <Request req> setJsonPayload (json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
public native function <Request req> getProperty (string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
public native function <Request req> removeHeader (string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"m: The message object" }
public native function <Request req> removeAllHeaders ();

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"m: The current message object" }
@doc:Param { value:"payload: The XML payload object" }
public native function <Request req> setXmlPayload (xml payload);

@doc:Description { value:"Clones and creates a new instance of a request message"}
@doc:Param { value:"req: A request message" }
@doc:Return { value:"request: The new instance of the request message" }
public native function <Request req> clone () (Request);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"req: A request message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
public native function <Request req> setHeader (string key, string value);



public struct Response {
}

@doc:Description { value:"Gets the HTTP status code from the message"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"int: http status code" }
public native function <Response res> getStatusCode () (int);

@doc:Description { value:"Gets the Content-Length header from the message"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"int: length of the message" }
public native function <Response res> getContentLength () (int);

@doc:Description { value:"Sets the Content-Length header on the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"contentLength: Length of the message" }
public native function <Response res> setContentLength (int contentLength);

@doc:Description { value:"Sets the HTTP StatusCode on the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"statusCode: HTTP status code" }
public native function <Response res> setStatusCode (int statusCode);

@doc:Description { value:"Sets a custom HTTP Reason phrase"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"reasonPhrase: Reason phrase value" }
public native function <Response res> setReasonPhrase (string reasonPhrase);

@doc:Description { value:"Gets the message payload in JSON format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"json: The JSON reresentation of the message payload" }
public native function <Response res> getJsonPayload () (json);

@doc:Description { value:"Gets the message payload in XML format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"xml: The XML representation of the message payload" }
public native function <Response res> getXmlPayload () (xml);

@doc:Description { value:"Gets the message payload in BLOB format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"json: The BLOB reresentation of the message payload" }
public native function <Response res> getBinaryPayload () (blob);

@doc:Description { value:"Sets a message property"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Param { value:"propertyValue: The value of the property" }
public native function <Response res> setProperty (string propertyName, string propertyValue);

@doc:Description { value:"Sets the message payload using a string object"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"payload: The string payload object" }
public native function <Response res> setStringPayload (string payload);

@doc:Description { value:"Gets a transport header from the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"headerName: The header name" }
@doc:Return { value:"string: The header value" }
public native function <Response res> getHeader (string headerName) (string);

@doc:Description { value:"Gets the message payload in string format"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"string: The string representation of the message payload" }
public native function <Response res> getStringPayload () (string);

@doc:Description { value:"Adds a transport header to the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
public native function <Response res> addHeader (string key, string value);

@doc:Description { value:"Gets transport headers from the message"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"string[]: The header values" }
public native function <Response res> getHeaders () (string[]);

@doc:Description { value:"Sets the message payload using a JSON object"}
@doc:Param { value:"req: The response message" }
@doc:Param { value:"payload: The JSON payload object" }
public native function <Response res> setJsonPayload (json payload);

@doc:Description { value:"Retrieve a message property"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"propertyName: The name of the property" }
@doc:Return { value:"string: The property value" }
public native function <Response res> getProperty (string propertyName) (string);

@doc:Description { value:"Removes a transport header from the message"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"key: The header name" }
public native function <Response res> removeHeader (string key);

@doc:Description { value:"Removes all transport headers from the message"}
@doc:Param { value:"res: The response message" }
public native function <Response res> removeAllHeaders ();

@doc:Description { value:"Sets the message payload using an XML object"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"payload: The XML payload object" }
public native function <Response res> setXmlPayload (xml payload);

@doc:Description { value:"Clones and creates a new instance of a response message"}
@doc:Param { value:"res: The response message" }
@doc:Return { value:"response: The new instance of the response message" }
public native function <Response res> clone () (Response);

@doc:Description { value:"Sets the value of a transport header"}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"key: The header name" }
@doc:Param { value:"value: The header value" }
public native function <Response res> setHeader (string key, string value);

@doc:Description { value:"Sends outbound response to the caller."}
@doc:Param { value:"res: The response message" }
public native function <Response res> send ();

@doc:Description { value:"Forwards client service response directly to the caller."}
@doc:Param { value:"res: The response message" }
@doc:Param { value:"resp: The new instance of the response message" }
public native function <Response res> forward (Response resp);

public struct Session {
}

@doc:Description { value:"Gets the session struct for valid id, otherwise create new" }
@doc:Param { value:"req: The request message" }
@doc:Return { value:"Session: HTTP session struct" }
public native function <Request req> createSessionIfAbsent () (Session);

@doc:Description { value:"Gets the session struct for valid id" }
@doc:Param { value:"req: The request message" }
@doc:Return { value:"Session: HTTP session struct" }
public native function <Request req> getSession () (Session);

@doc:Description { value:"Gets the session attribute" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"attributeKey: HTTPSession attribute key" }
@doc:Return { value:"any: HTTPSession attribute value" }
public native function <Session session> getAttribute (string attributeKey) (any);

@doc:Description { value:"Sets session attributes to the message" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"attributeKey: HTTPSession attribute key" }
@doc:Param { value:"attributeValue: HTTPSession attribute Value" }
public native function <Session session> setAttribute (string attributeKey, any attributeValue);

@doc:Description { value:"Gets the session attribute names" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"string[]: HTTPSession attribute name array" }
public native function <Session session> getAttributeNames () (string[]);

@doc:Description { value:"Gets the session attribute" }
@doc:Param { value:"session: A session struct" }
public native function <Session session> invalidate ();

@doc:Description { value:"Remove the session attribute" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"attributeKey: HTTPSession attribute key" }
public native function <Session session> removeAttribute (string attributeKey);

@doc:Description { value:"Gets the session id" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"string: HTTPSession id" }
public native function <Session session> getId () (string);

@doc:Description { value:"Gets the session status" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"boolean: HTTPSession status" }
public native function <Session session> isNew () (boolean);

@doc:Description { value:"Gets the session creation time" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"int: HTTPSession creation time" }
public native function <Session session> getCreationTime () (int);

@doc:Description { value:"Gets the session last accessed time" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"int: HTTPSession last accessed time" }
public native function <Session session> getLastAccessedTime () (int);

@doc:Description { value:"Gets the session max inactive interval" }
@doc:Param { value:"session: A session struct" }
@doc:Return { value:"int: HTTPSession max inactive interval" }
public native function <Session session> getMaxInactiveInterval () (int);

@doc:Description { value:"Sets session max inactive interval" }
@doc:Param { value:"session: A session struct" }
@doc:Param { value:"timeInterval: HTTPSession max inactive interval" }
public native function <Session session> setMaxInactiveInterval (int timeInterval);


struct SSL {
    string trustStoreFile;
    string trustStorePassword;
    string keyStoreFile;
    string keyStorePassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
}

struct FollowRedirects {
    boolean enabled = false;
    int maxCount = 5;
}

public struct Options {
    int port;
    int endpointTimeout = 60000;
    boolean chunkDisabled = false;
    FollowRedirects followRedirects;
    SSL ssl;
}

public connector ClientConnector (string serviceUri, Options connectorOptions) {

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