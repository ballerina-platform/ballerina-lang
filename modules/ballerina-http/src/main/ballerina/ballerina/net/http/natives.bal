package ballerina.net.http;

public struct Request {
}

@Description { value:"Gets the request URL from the message"}
@Param { value:"req: The request message" }
@Return { value:"string: The request URL value" }
public native function <Request req> getRequestURL () (string);

@Description { value:"Gets the Content-Length header from the message"}
@Param { value:"req: A request message" }
@Return { value:"int: length of the message" }
public native function <Request req> getContentLength () (int);

@Description { value:"Gets the HTTP method from the message"}
@Param { value:"req: A request message" }
@Return { value:"string: http method value" }
public native function <Request req> getMethod () (string);

@Description { value:"Sets the Content-Length header on the message"}
@Param { value:"req: A request message" }
@Param { value:"contentLength: Length of the message" }
public native function <Request req> setContentLength (int contentLength);

@Description { value:"Gets formParam map from HTTP message"}
@Param { value:"req: The request message" }
@Return { value:"map: The map of form params" }
public native function <Request req> getFormParams () (map);

@Description { value:"Gets queryParam map from HTTP message"}
@Param { value:"req: The request message" }
@Return { value:"map: The map of query params" }
public native function <Request req> getQueryParams () (map);

@Description { value:"Gets the message payload in JSON format"}
@Param { value:"req: A request message" }
@Return { value:"json: The JSON reresentation of the message payload" }
public native function <Request req> getJsonPayload () (json);

@Description { value:"Gets the message payload in XML format"}
@Param { value:"req: The request message" }
@Return { value:"xml: The XML representation of the message payload" }
public native function <Request req> getXmlPayload () (xml);

@Description { value:"Gets the message payload in BLOB format"}
@Param { value:"req: A request message" }
@Return { value:"json: The BLOB reresentation of the message payload" }
public native function <Request req> getBinaryPayload () (blob);

@Description { value:"Sets a message property"}
@Param { value:"req: A request message" }
@Param { value:"propertyName: The name of the property" }
@Param { value:"propertyValue: The value of the property" }
public native function <Request req> setProperty (string propertyName, string propertyValue);

@Description { value:"Sets the message payload using a string object"}
@Param { value:"req: A request message" }
@Param { value:"payload: The string payload object" }
public native function <Request req> setStringPayload (string payload);

@Description { value:"Gets a transport header from the message"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Return { value:"string: The header value" }
public native function <Request req> getHeader (string headerName) (string);

@Description { value:"Gets the message payload in string format"}
@Param { value:"req: A request message" }
@Return { value:"string: The string representation of the message payload" }
public native function <Request req> getStringPayload () (string);

@Description { value:"Adds a transport header to the message"}
@Param { value:"m: The message object" }
@Param { value:"key: The header name" }
@Param { value:"value: The header value" }
public native function <Request req> addHeader (string key, string value);

@Description { value:"Gets transport headers from the message"}
@Param { value:"req: A request message" }
@Return { value:"string[]: The header values" }
public native function <Request req> getHeaders () (string[]);

@Description { value:"Sets the message payload using a JSON object"}
@Param { value:"req: A request message" }
@Param { value:"payload: The JSON payload object" }
public native function <Request req> setJsonPayload (json payload);

@Description { value:"Retrieve a message property"}
@Param { value:"req: A request message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"string: The property value" }
public native function <Request req> getProperty (string propertyName) (string);

@Description { value:"Removes a transport header from the message"}
@Param { value:"req: A request message" }
@Param { value:"key: The header name" }
public native function <Request req> removeHeader (string key);

@Description { value:"Removes all transport headers from the message"}
@Param { value:"m: The message object" }
public native function <Request req> removeAllHeaders ();

@Description { value:"Sets the message payload using an XML object"}
@Param { value:"m: The current message object" }
@Param { value:"payload: The XML payload object" }
public native function <Request req> setXmlPayload (xml payload);

@Description { value:"Clones and creates a new instance of a request message"}
@Param { value:"req: A request message" }
@Return { value:"request: The new instance of the request message" }
public native function <Request req> clone () (Request);

@Description { value:"Sets the value of a transport header"}
@Param { value:"req: A request message" }
@Param { value:"key: The header name" }
@Param { value:"value: The header value" }
public native function <Request req> setHeader (string key, string value);



public struct Response {
}

@Description { value:"Gets the HTTP status code from the message"}
@Param { value:"res: The response message" }
@Return { value:"int: http status code" }
public native function <Response res> getStatusCode () (int);

@Description { value:"Gets the Content-Length header from the message"}
@Param { value:"res: The response message" }
@Return { value:"int: length of the message" }
public native function <Response res> getContentLength () (int);

@Description { value:"Sets the Content-Length header on the message"}
@Param { value:"res: The response message" }
@Param { value:"contentLength: Length of the message" }
public native function <Response res> setContentLength (int contentLength);

@Description { value:"Sets the HTTP StatusCode on the message"}
@Param { value:"res: The response message" }
@Param { value:"statusCode: HTTP status code" }
public native function <Response res> setStatusCode (int statusCode);

@Description { value:"Sets a custom HTTP Reason phrase"}
@Param { value:"res: The response message" }
@Param { value:"reasonPhrase: Reason phrase value" }
public native function <Response res> setReasonPhrase (string reasonPhrase);

@Description { value:"Gets the message payload in JSON format"}
@Param { value:"res: The response message" }
@Return { value:"json: The JSON reresentation of the message payload" }
public native function <Response res> getJsonPayload () (json);

@Description { value:"Gets the message payload in XML format"}
@Param { value:"res: The response message" }
@Return { value:"xml: The XML representation of the message payload" }
public native function <Response res> getXmlPayload () (xml);

@Description { value:"Gets the message payload in BLOB format"}
@Param { value:"res: The response message" }
@Return { value:"json: The BLOB reresentation of the message payload" }
public native function <Response res> getBinaryPayload () (blob);

@Description { value:"Sets a message property"}
@Param { value:"res: The response message" }
@Param { value:"propertyName: The name of the property" }
@Param { value:"propertyValue: The value of the property" }
public native function <Response res> setProperty (string propertyName, string propertyValue);

@Description { value:"Sets the message payload using a string object"}
@Param { value:"res: The response message" }
@Param { value:"payload: The string payload object" }
public native function <Response res> setStringPayload (string payload);

@Description { value:"Gets a transport header from the message"}
@Param { value:"res: The response message" }
@Param { value:"headerName: The header name" }
@Return { value:"string: The header value" }
public native function <Response res> getHeader (string headerName) (string);

@Description { value:"Gets the message payload in string format"}
@Param { value:"res: The response message" }
@Return { value:"string: The string representation of the message payload" }
public native function <Response res> getStringPayload () (string);

@Description { value:"Adds a transport header to the message"}
@Param { value:"res: The response message" }
@Param { value:"key: The header name" }
@Param { value:"value: The header value" }
public native function <Response res> addHeader (string key, string value);

@Description { value:"Gets transport headers from the message"}
@Param { value:"res: The response message" }
@Return { value:"string[]: The header values" }
public native function <Response res> getHeaders () (string[]);

@Description { value:"Sets the message payload using a JSON object"}
@Param { value:"req: The response message" }
@Param { value:"payload: The JSON payload object" }
public native function <Response res> setJsonPayload (json payload);

@Description { value:"Retrieve a message property"}
@Param { value:"res: The response message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"string: The property value" }
public native function <Response res> getProperty (string propertyName) (string);

@Description { value:"Removes a transport header from the message"}
@Param { value:"res: The response message" }
@Param { value:"key: The header name" }
public native function <Response res> removeHeader (string key);

@Description { value:"Removes all transport headers from the message"}
@Param { value:"res: The response message" }
public native function <Response res> removeAllHeaders ();

@Description { value:"Sets the message payload using an XML object"}
@Param { value:"res: The response message" }
@Param { value:"payload: The XML payload object" }
public native function <Response res> setXmlPayload (xml payload);

@Description { value:"Clones and creates a new instance of a response message"}
@Param { value:"res: The response message" }
@Return { value:"response: The new instance of the response message" }
public native function <Response res> clone () (Response);

@Description { value:"Sets the value of a transport header"}
@Param { value:"res: The response message" }
@Param { value:"key: The header name" }
@Param { value:"value: The header value" }
public native function <Response res> setHeader (string key, string value);

@Description { value:"Sends outbound response to the caller."}
@Param { value:"res: The response message" }
public native function <Response res> send ();

@Description { value:"Forwards client service response directly to the caller."}
@Param { value:"res: The response message" }
@Param { value:"resp: The new instance of the response message" }
public native function <Response res> forward (Response resp);

public struct Session {
}

@Description { value:"Gets the session struct for valid id, otherwise create new" }
@Param { value:"req: The request message" }
@Return { value:"Session: HTTP session struct" }
public native function <Request req> createSessionIfAbsent () (Session);

@Description { value:"Gets the session struct for valid id" }
@Param { value:"req: The request message" }
@Return { value:"Session: HTTP session struct" }
public native function <Request req> getSession () (Session);

@Description { value:"Gets the session attribute" }
@Param { value:"session: A session struct" }
@Param { value:"attributeKey: HTTPSession attribute key" }
@Return { value:"any: HTTPSession attribute value" }
public native function <Session session> getAttribute (string attributeKey) (any);

@Description { value:"Sets session attributes to the message" }
@Param { value:"session: A session struct" }
@Param { value:"attributeKey: HTTPSession attribute key" }
@Param { value:"attributeValue: HTTPSession attribute Value" }
public native function <Session session> setAttribute (string attributeKey, any attributeValue);

@Description { value:"Gets the session attribute names" }
@Param { value:"session: A session struct" }
@Return { value:"string[]: HTTPSession attribute name array" }
public native function <Session session> getAttributeNames () (string[]);

@Description { value:"Gets the session attribute" }
@Param { value:"session: A session struct" }
public native function <Session session> invalidate ();

@Description { value:"Remove the session attribute" }
@Param { value:"session: A session struct" }
@Param { value:"attributeKey: HTTPSession attribute key" }
public native function <Session session> removeAttribute (string attributeKey);

@Description { value:"Gets the session id" }
@Param { value:"session: A session struct" }
@Return { value:"string: HTTPSession id" }
public native function <Session session> getId () (string);

@Description { value:"Gets the session status" }
@Param { value:"session: A session struct" }
@Return { value:"boolean: HTTPSession status" }
public native function <Session session> isNew () (boolean);

@Description { value:"Gets the session creation time" }
@Param { value:"session: A session struct" }
@Return { value:"int: HTTPSession creation time" }
public native function <Session session> getCreationTime () (int);

@Description { value:"Gets the session last accessed time" }
@Param { value:"session: A session struct" }
@Return { value:"int: HTTPSession last accessed time" }
public native function <Session session> getLastAccessedTime () (int);

@Description { value:"Gets the session max inactive interval" }
@Param { value:"session: A session struct" }
@Return { value:"int: HTTPSession max inactive interval" }
public native function <Session session> getMaxInactiveInterval () (int);

@Description { value:"Sets session max inactive interval" }
@Param { value:"session: A session struct" }
@Param { value:"timeInterval: HTTPSession max inactive interval" }
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

	@Description { value:"The POST action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action post (string path, Request req) (Response);

	@Description { value:"The HEAD action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action head (string path, Request req) (Response);

	@Description { value:"The PUT action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action put (string path, Request req) (Response);

	@Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@Param { value:"httpVerb: HTTP verb value" }
	@Param { value:"path: Resource path " }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action execute (string httpVerb, string path, Request req) (Response);

	@Description { value:"The PATCH action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action patch (string path, Request req) (Response);

	@Description { value:"The DELETE action implementation of the HTTP connector"}
	@Param { value:"path: Resource path " }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action delete (string path, Request req) (Response);

	@Description { value:"GET action implementation of the HTTP Connector"}
	@Param { value:"path: Request path" }
	@Param { value:"req: A request message" }
	@Return { value:"response: The response message" }
	native action get (string path, Request req) (Response);
}