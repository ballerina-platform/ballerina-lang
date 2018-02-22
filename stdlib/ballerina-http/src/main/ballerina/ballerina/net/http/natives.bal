package ballerina.net.http;

import ballerina.mime;

@Description {value:"Represent 'Content-Legth' header name"}
public const string CONTENT_LENGTH = "Content-Length";

@Description { value:"Represents the HTTP server connector connection"}
@Field {value:"remoteHost: The server host name"}
@Field {value:"port: The server port"}
public struct Connection {
	string remoteHost;
	int port;
}

@Description { value:"Sends outbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during HTTP server connector respond" }
public native function <Connection conn> respond (OutResponse res) (HttpConnectorError);

@Description { value:"Forwards inbound response to the caller"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The inbound response message" }
@Return { value:"Error occured during HTTP server connector forward" }
public native function <Connection conn> forward (InResponse res) (HttpConnectorError);

@Description { value:"Gets the Session struct for a valid session cookie from the connection. Otherwise creates a new Session struct." }
@Param { value:"conn: The server connector connection" }
@Return { value:"HTTP Session struct" }
public native function <Connection conn> createSessionIfAbsent () (Session);

@Description { value:"Gets the Session struct from the connection if it is present" }
@Param { value:"conn: The server connector connection" }
@Return { value:"The HTTP Session struct assoicated with the request" }
public native function <Connection conn> getSession () (Session);

@Description { value:"Represents an HTTP inbound request message"}
@Field {value:"path: Resource path of request URI"}
@Field {value:"method: HTTP request method"}
@Field {value:"httpVersion: The version of HTTP"}
@Field {value:"userAgent: User-Agent request header"}
public struct InRequest {
	string rawPath;
	string method;
	string httpVersion;
	string userAgent;
    string extraPathInfo;
}

@Description { value:"Get the entity from the inbound request with the body included"}
@Param { value:"req: The inbound request message" }
@Return { value:"Entity of the request" }
public native function <InRequest req> getEntity () (mime:Entity);

@Description { value:"Get the entity from the inbound request without the body. This function is to be used only internally"}
@Param { value:"req: The inbound request message" }
@Return { value:"Entity of the request" }
native function <InRequest req> getEntityWithoutBody () (mime:Entity);

@Description { value:"Set the entity to inbound request"}
@Param { value:"req: The inbound request message" }
@Return { value:"Entity of the request" }
public native function <InRequest req> setEntity (mime:Entity entity);

@Description { value:"Gets the query parameters from the HTTP request as a map"}
@Param { value:"req: The inbound request message" }
@Return { value:"The map of query params" }
public native function <InRequest req> getQueryParams () (map);

@Description { value:"Retrieves the named property from the request"}
@Param { value:"req: The inbound request message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"The property value" }
public native function <InRequest req> getProperty (string propertyName) (string);

@Description { value: "Get matrix parameters from the request"}
@Param { value:"req: The inbound request message" }
@Param { value: "path: Path to the location of matrix parameters"}
@Return { value: "A map of matrix paramters which can be found for a given path"}
public native function <InRequest req> getMatrixParams (string path) (map);

@Description { value:"Represents an HTTP outbound request message"}
public struct OutRequest {
}

@Description { value:"Get the entity from the outbound request"}
@Param { value:"req: The outbound request message" }
@Return { value:"Entity of the request" }
public native function <OutRequest req> getEntity () (mime:Entity);

@Description { value:"Get the entity from the outbound request without the body. This function is to be used only internally"}
@Param { value:"req: The outbound request message" }
@Return { value:"Entity of the request" }
native function <OutRequest req> getEntityWithoutBody () (mime:Entity);

@Description { value:"Set the entity to outbound request"}
@Param { value:"req: The outbound request message" }
@Return { value:"Entity of the request" }
public native function <OutRequest req> setEntity (mime:Entity entity);

@Description { value:"Sets a request property"}
@Param { value:"req: The outbound request message" }
@Param { value:"propertyName: The name of the property" }
@Param { value:"propertyValue: The value of the property" }
public native function <OutRequest req> setProperty (string propertyName, string propertyValue);

@Description { value:"Retrieves the named property from the request"}
@Param { value:"req: The outbound request message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"The property value" }
public native function <OutRequest req> getProperty (string propertyName) (string);

@Description { value:"Represents an HTTP Inbound response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
@Field {value:"server: The server header"}
public struct InResponse {
    int statusCode;
    string reasonPhrase;
    string server;
}

@Description { value:"Get the entity from the inbound response with the body"}
@Param { value:"res: The inbound response message" }
@Return { value:"Entity of the response" }
public native function <InResponse res> getEntity () (mime:Entity);

@Description { value:"Get the entity from the inbound response without the body. This function is to be used only internally"}
@Param { value:"req: The inbound response message" }
@Return { value:"Entity of the response" }
native function <InResponse res> getEntityWithoutBody () (mime:Entity);

@Description { value:"Set the entity to inbound response"}
@Param { value:"res: The inbound response message" }
@Return { value:"Entity of the response" }
public native function <InResponse res> setEntity (mime:Entity entity);

@Description { value:"Retrieve a response property"}
@Param { value:"res: The inbound response message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"The property value" }
public native function <InResponse res> getProperty (string propertyName) (string);

@Description { value:"Represents an HTTP outbound response message"}
@Field {value:"statusCode: The response status code"}
@Field {value:"reasonPhrase: The status code reason phrase"}
public struct OutResponse {
    int statusCode;
    string reasonPhrase;
}

@Description { value:"Get the entity from the outbound response"}
@Param { value:"res: The outbound response message" }
@Return { value:"Entity of the response" }
public native function <OutResponse res> getEntity () (mime:Entity);

@Description { value:"Get the entity from the outbound response without the body. This function is to be used only internally"}
@Param { value:"req: The outbound response message" }
@Return { value:"Entity of the response" }
native function <OutResponse res> getEntityWithoutBody () (mime:Entity);

@Description { value:"Set the entity to outbound response"}
@Param { value:"res: The outbound response message" }
@Return { value:"Entity of the response" }
public native function <OutResponse res> setEntity (mime:Entity entity);

@Description { value:"Sets a response property"}
@Param { value:"res: The outbound response message" }
@Param { value:"propertyName: The name of the property" }
@Param { value:"propertyValue: The value of the property" }
public native function <OutResponse res> setProperty (string propertyName, string propertyValue);

@Description { value:"Retrieve a response property"}
@Param { value:"res: The outbound response message" }
@Param { value:"propertyName: The name of the property" }
@Return { value:"The property value" }
public native function <OutResponse res> getProperty (string propertyName) (string);

@Description { value:"Parse headerValue and return value with parameter map"}
@Param { value:"headerValue: The header value" }
@Return { value:"The header value" }
@Return { value:"The header value parameter map" }
@Return { value:"Error occured during header parsing" }
public native function parseHeader (string headerValue)(string, map, error);

@Description { value:"Represents an HTTP Session"}
public struct Session {
}

@Description { value:"Gets the named session attribute" }
@Param { value:"session: A Session struct" }
@Param { value:"attributeKey: HTTP session attribute key" }
@Return { value:"HTTP session attribute value" }
public native function <Session session> getAttribute (string attributeKey) (any);

@Description { value:"Sets the specified key/value pair as a session attribute" }
@Param { value:"session: A Session struct" }
@Param { value:"attributeKey: Session attribute key" }
@Param { value:"attributeValue: Session attribute Value" }
public native function <Session session> setAttribute (string attributeKey, any attributeValue);

@Description { value:"Gets the session attribute names" }
@Param { value:"session: A Session struct" }
@Return { value:"Session attribute names array" }
public native function <Session session> getAttributeNames () (string[]);

@Description { value:"Gets the session attribute key value pairs as a map" }
@Param { value:"session: A session struct" }
@Return { value:"The map of session attributes key value pairs" }
public native function <Session session> getAttributes () (map);

@Description { value:"Invalidates the session and it will no longer be accessible from the request" }
@Param { value:"session: A Session struct" }
public native function <Session session> invalidate ();

@Description { value:"Remove the named session attribute" }
@Param { value:"session: A Session struct" }
@Param { value:"attributeKey: Session attribute key" }
public native function <Session session> removeAttribute (string attributeKey);

@Description { value:"Gets the session cookie ID" }
@Param { value:"session: A Session struct" }
@Return { value:"Session ID" }
public native function <Session session> getId () (string);

@Description { value:"Checks whether the given session is a newly created session or an existing session" }
@Param { value:"session: A Session struct" }
@Return { value:"Indicates if the session is a newly created session or not" }
public native function <Session session> isNew () (boolean);

@Description { value:"Gets the session creation time" }
@Param { value:"session: A Session struct" }
@Return { value:"Session creation time" }
public native function <Session session> getCreationTime () (int);

@Description { value:"Gets the last time the sessions was accessed" }
@Param { value:"session: A Session struct" }
@Return { value:"Last accessed time of the session" }
public native function <Session session> getLastAccessedTime () (int);

@Description { value:"Gets maximum inactive interval for the session. The session expires after this time period." }
@Param { value:"session: A Session struct" }
@Return { value:"Session max inactive interval" }
public native function <Session session> getMaxInactiveInterval () (int);

@Description { value:"Sets the maximum inactive interval for the session. The session expires after this time period." }
@Param { value:"session: A Session struct" }
@Param { value:"timeInterval: Session max inactive interval" }
public native function <Session session> setMaxInactiveInterval (int timeInterval);

@Description { value:"HttpConnectorError struct represents an error occured during the HTTP client invocation" }
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error that caused HttpConnectorError to get thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct HttpConnectorError {
    string message;
    error cause;
    int statusCode;
}

@Description { value:"Retry struct represents retry related options for HTTP client invocation" }
@Field {value:"count: Number of retries"}
@Field {value:"interval: Retry interval in millisecond"}
public struct Retry {
    int count;
    int interval;
}

@Description { value:"SSL struct represents SSL/TLS options to be used for HTTP client invocation" }
@Field {value:"trustStoreFile: File path to trust store file"}
@Field {value:"trustStorePassword: Trust store password"}
@Field {value:"keyStoreFile: File path to key store file"}
@Field {value:"keyStorePassword: Key store password"}
@Field {value:"sslEnabledProtocols: SSL/TLS protocols to be enabled. eg: TLSv1,TLSv1.1,TLSv1.2"}
@Field {value:"ciphers: List of ciphers to be used. eg: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"}
@Field {value:"sslProtocol: SSL Protocol to be used. eg: TLS1.2"}
@Field {value:"validateCertEnabled: The status of validateCertEnabled {default value : false (disable)}"}
@Field {value:"cacheSize: Maximum size of the cache"}
@Field {value:"cacheValidityPeriod: Time duration of cache validity period"}
@Field {value:"hostNameVerificationEnabled: Enable/disable host name verification. {default value : true (enable)}"}
public struct SSL {
    string trustStoreFile;
    string trustStorePassword;
    string keyStoreFile;
    string keyStorePassword;
    string sslEnabledProtocols;
    string ciphers;
    string sslProtocol;
    boolean validateCertEnabled;
    int cacheSize;
    int cacheValidityPeriod;
    boolean hostNameVerificationEnabled;
}

@Description { value:"FollowRedirects struct represents HTTP redirect related options to be used for HTTP client invocation" }
@Field {value:"enabled: Enable redirect"}
@Field {value:"maxCount: Maximun number of redirects to follow"}
public struct FollowRedirects {
    boolean enabled = false;
    int maxCount = 5;
}

@Description { value:"Proxy struct represents proxy server configurations to be used for HTTP client invocation" }
@Field {value:"proxyHost: host name of the proxy server"}
@Field {value:"proxyPort: proxy server port"}
@Field {value:"proxyUserName: Proxy server user name"}
@Field {value:"proxyPassword: proxy server password"}
public struct Proxy {
    string host;
    int port;
    string userName;
    string password;
}

@Description { value:"Options struct represents options to be used for HTTP client invocation" }
@Field {value:"port: Port number of the remote service"}
@Field {value:"endpointTimeout: Endpoint timeout value in millisecond (default value: 60000 milliseconds)"}
@Field {value:"maxActiveConnections: The maximum number of active connections the connector can create (default value: -1, indicates that the number of connections is not restricted)"}
@Field {value:"keepAlive: Keep the connection or close it (default value: true)"}
@Field {value:"transferEncoding: The types of encoding applied to the request (default value: chunking)"}
@Field {value:"chunking: The chunking behaviour of the request"}
@Field {value:"httpVersion: The version of HTTP outbound request"}
@Field {value:"followRedirects: Redirect related options"}
@Field {value:"ssl: SSL/TLS related options"}
@Field {value:"retryConfig: Retry related options"}
@Field {value:"proxy: Proxy server related options"}
public struct Options {
    int port;
    int endpointTimeout = 60000;
    int maxActiveConnections = -1;
    boolean keepAlive = true;
    string transferEncoding = "chunking";
    string chunking = "auto";
    string httpVersion;
    FollowRedirects followRedirects;
    SSL ssl;
    Retry retryConfig;
    Proxy proxy;
}

@Description { value:"HTTP client connector for outbound HTTP requests"}
@Param { value:"serviceUri: URI of the service" }
@Param { value:"connectorOptions: connector options" }
public connector HttpClient (string serviceUri, Options connectorOptions) {

	@Description { value:"The POST action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action post (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"The HEAD action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action head (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"The PUT action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action put (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"Invokes an HTTP call with the specified HTTP verb."}
	@Param { value:"httpVerb: HTTP verb value" }
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action execute (string httpVerb, string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"The PATCH action implementation of the HTTP Connector."}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action patch (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"The DELETE action implementation of the HTTP connector"}
	@Param { value:"path: Resource path " }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action delete (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"GET action implementation of the HTTP Connector"}
	@Param { value:"path: Request path" }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action get (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"OPTIONS action implementation of the HTTP Connector"}
	@Param { value:"path: Request path" }
	@Param { value:"req: An HTTP outbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action options (string path, OutRequest req) (InResponse, HttpConnectorError);

	@Description { value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
	@Param { value:"path: Request path" }
	@Param { value:"req: An HTTP inbound request message" }
	@Return { value:"The inbound response message" }
	@Return { value:"Error occured during HTTP client invocation" }
	native action forward (string path, InRequest req) (InResponse, HttpConnectorError);
}