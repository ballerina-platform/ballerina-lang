package ballerina.net.http;

@Description { value:"Represents a HTTP/2 Push Promise"}
@Field {value:"path: Resource path"}
@Field {value:"method: Http method"}
public struct PushPromise {
    string path;
    string method;
}

//////////////////////////////
/// Native implementations ///
//////////////////////////////

@Description {value:"Returns the header value with the specified header name. If there are more than one header value for the specified header name, the first value is returned."}
@Param {value:"promise: A Push Promise"}
@Param {value:"headerName: The header name"}
@Return {value:"The first header value for the provided header name. Returns null if the header does not exist."}
public native function <PushPromise promise> getHeader (string headerName) (string);

@Description {value:"Gets transport headers from the Push Promise"}
@Param {value:"promise: A inbound request message"}
@Param {value:"headerName: The header name"}
@Return {value:"The header values struct array for a given header name"}
public native function <PushPromise promise> getHeaders (string headerName) (string[]);

@Description {value:"Adds the specified key/value pair as an HTTP header to the Push Promise"}
@Param {value:"promise: A Push Promise"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public native function <PushPromise promise> addHeader (string headerName, string headerValue);

@Description {value:"Sets the value of a transport header in Push Promise"}
@Param {value:"promise: A Push Promise"}
@Param {value:"headerName: The header name"}
@Param {value:"headerValue: The header value"}
public native function <PushPromise promise> setHeader (string headerName, string headerValue);

@Description {value:"Removes a transport header from the Push Promise"}
@Param {value:"promise: A Push Promise"}
@Param {value:"key: The header name"}
public native function <PushPromise promise> removeHeader (string key);

@Description {value:"Removes all transport headers from the Push Promise"}
@Param {value:"promise: A Push Promise"}
public native function <PushPromise promise> removeAllHeaders ();
